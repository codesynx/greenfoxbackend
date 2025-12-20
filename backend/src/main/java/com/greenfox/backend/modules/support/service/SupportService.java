package com.greenfox.backend.modules.support.service;

import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.common.exception.ForbiddenException;
import com.greenfox.backend.common.exception.ResourceNotFoundException;
import com.greenfox.backend.modules.support.dto.ConversationResponse;
import com.greenfox.backend.modules.support.dto.SendMessageRequest;
import com.greenfox.backend.modules.support.dto.SupportMessageResponse;
import com.greenfox.backend.modules.support.dto.TicketResponse;
import com.greenfox.backend.modules.support.entity.SupportMessage;
import com.greenfox.backend.modules.support.repository.SupportMessageRepository;
import com.greenfox.backend.modules.user.entity.User;
import com.greenfox.backend.modules.user.repository.UserRepository;
import com.greenfox.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Map;

/**
 * Service for support chat/ticket management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportMessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * Send a message from user to support.
     * Creates a new ticket (conversation) for each message.
     */
    @Transactional
    public SupportMessageResponse sendMessage(UserPrincipal principal, SendMessageRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        // Generate unique conversation ID for each new ticket
        String conversationId = generateUniqueConversationId();

        SupportMessage message = SupportMessage.builder()
                .user(user)
                .conversationId(conversationId)
                .subject(request.getSubject() != null ? request.getSubject().trim() : null)
                .category(request.getCategory() != null ? request.getCategory().trim().toUpperCase() : null)
                .message(request.getMessage())
                .fromAdmin(false)
                .build();

        SupportMessage saved = messageRepository.save(message);
        log.info("Support message sent by user: {} in conversation: {}", user.getId(), conversationId);

        return toResponse(saved);
    }

    /**
     * Get chat history for user for a specific ticket.
     */
    @Transactional
    public PageResponse<SupportMessageResponse> getHistory(UserPrincipal principal, String ticketId, Pageable pageable) {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new com.greenfox.backend.common.exception.BadRequestException("ticketId parameter is required");
        }
        
        String conversationId = ticketId.trim();
        
        // Verify that this conversation belongs to the user
        Page<SupportMessage> messages = messageRepository
                .findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(conversationId, pageable);
        
        if (!messages.isEmpty() && !messages.getContent().get(0).getUser().getId().equals(principal.getId())) {
            throw new ForbiddenException("Access denied to this conversation");
        }

        // Mark admin messages as read
        messageRepository.markConversationAsRead(conversationId);

        List<SupportMessageResponse> content = messages.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.from(messages, content);
    }

    // ============== Admin Operations ==============

    /**
     * Get all conversations (for admin).
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations() {
        List<SupportMessage> latestMessages = messageRepository.findLatestMessagePerConversation();

        return latestMessages.stream()
                .map(this::toConversationResponse)
                .sorted(Comparator.comparing(ConversationResponse::getLastActivityAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get messages for a specific user (for admin).
     */
    @Transactional
    public PageResponse<SupportMessageResponse> getConversation(UUID userId, Pageable pageable) {
        String conversationId = generateConversationId(userId);
        
        Page<SupportMessage> messages = messageRepository
                .findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(conversationId, pageable);

        // Mark user messages as read (admin viewed)
        messageRepository.markUserMessagesAsRead(conversationId);

        List<SupportMessageResponse> content = messages.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.from(messages, content);
    }

    /**
     * Reply to a user (admin only).
     */
    @Transactional
    public SupportMessageResponse replyToUser(UUID userId, UserPrincipal adminPrincipal, SendMessageRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        User admin = userRepository.findByIdAndDeletedFalse(adminPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "id", adminPrincipal.getId()));

        String conversationId = generateConversationId(userId);

        SupportMessage message = SupportMessage.builder()
                .user(user)
                .conversationId(conversationId)
                .message(request.getMessage())
                .fromAdmin(true)
                .admin(admin)
                .build();

        SupportMessage saved = messageRepository.save(message);
        log.info("Admin {} replied to user: {}", admin.getId(), userId);

        return toResponse(saved);
    }

    /**
     * Get unread message count (for admin badge).
     */
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return messageRepository.countUnreadFromUsers();
    }

    /**
     * Generate a unique conversation ID for a new ticket.
     */
    private String generateUniqueConversationId() {
        return "conv-" + UUID.randomUUID().toString();
    }

    /**
     * Generate conversation ID for legacy support (admin operations).
     * Used for backward compatibility with admin endpoints.
     */
    private String generateConversationId(UUID userId) {
        return "conv-" + userId.toString();
    }

    private SupportMessageResponse toResponse(SupportMessage message) {
        return SupportMessageResponse.builder()
                .id(message.getId())
                .subject(message.getSubject())
                .category(message.getCategory())
                .message(message.getMessage())
                .fromAdmin(message.isFromAdmin())
                .adminName(message.getAdmin() != null ? message.getAdmin().getName() : null)
                .read(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }

    /**
     * Get all tickets for the current user (grouped by conversation).
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets(UserPrincipal principal) {
        Page<SupportMessage> userMessagesPage = messageRepository
                .findByUserIdAndDeletedFalseOrderByCreatedAtDesc(
                        principal.getId(), 
                        org.springframework.data.domain.PageRequest.of(0, 1000)
                );
        List<SupportMessage> userMessages = userMessagesPage.getContent();

        // Group by conversation ID and get latest message per conversation
        Map<String, SupportMessage> latestByConversation = new java.util.LinkedHashMap<>();
        for (SupportMessage msg : userMessages) {
            String convId = msg.getConversationId();
            if (!latestByConversation.containsKey(convId)) {
                latestByConversation.put(convId, msg);
            }
        }

        // Convert to TicketResponse and calculate unread counts per conversation
        return latestByConversation.values().stream()
                .map(msg -> {
                    long unread = messageRepository.countUnreadInConversation(msg.getConversationId());
                    // Get the first message in this conversation for createdAt
                    SupportMessage firstMessage = userMessages.stream()
                            .filter(m -> m.getConversationId().equals(msg.getConversationId()))
                            .reduce((first, second) -> first.getCreatedAt().isBefore(second.getCreatedAt()) ? first : second)
                            .orElse(msg);

                    return TicketResponse.builder()
                            .ticketId(msg.getConversationId())
                            .subject(msg.getSubject())
                            .category(msg.getCategory())
                            .lastMessage(truncate(msg.getMessage(), 100))
                            .unreadCount((int) unread)
                            .lastActivityAt(msg.getCreatedAt())
                            .createdAt(firstMessage.getCreatedAt())
                            .build();
                })
                .sorted(java.util.Comparator.comparing(TicketResponse::getLastActivityAt).reversed())
                .collect(java.util.stream.Collectors.toList());
    }

    private ConversationResponse toConversationResponse(SupportMessage lastMessage) {
        User user = lastMessage.getUser();
        
        // Count unread from this user
        long unread = messageRepository.countByUserIdAndFromAdminTrueAndReadFalseAndDeletedFalse(user.getId());

        return ConversationResponse.builder()
                .conversationId(lastMessage.getConversationId())
                .userId(user.getId())
                .userPhone(user.getPhoneNumber())
                .userName(user.getName())
                .subject(lastMessage.getSubject())
                .category(lastMessage.getCategory())
                .lastMessage(truncate(lastMessage.getMessage(), 100))
                .lastFromAdmin(lastMessage.isFromAdmin())
                .unreadCount((int) unread)
                .lastActivityAt(lastMessage.getCreatedAt())
                .build();
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}

