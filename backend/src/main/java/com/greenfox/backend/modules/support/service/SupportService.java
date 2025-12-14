package com.greenfox.backend.modules.support.service;

import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.common.exception.ResourceNotFoundException;
import com.greenfox.backend.modules.support.dto.ConversationResponse;
import com.greenfox.backend.modules.support.dto.SendMessageRequest;
import com.greenfox.backend.modules.support.dto.SupportMessageResponse;
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
     */
    @Transactional
    public SupportMessageResponse sendMessage(UserPrincipal principal, SendMessageRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        String conversationId = generateConversationId(user.getId());

        SupportMessage message = SupportMessage.builder()
                .user(user)
                .conversationId(conversationId)
                .message(request.getMessage())
                .fromAdmin(false)
                .build();

        SupportMessage saved = messageRepository.save(message);
        log.info("Support message sent by user: {}", user.getId());

        return toResponse(saved);
    }

    /**
     * Get chat history for user.
     */
    @Transactional
    public PageResponse<SupportMessageResponse> getHistory(UserPrincipal principal, Pageable pageable) {
        String conversationId = generateConversationId(principal.getId());
        
        Page<SupportMessage> messages = messageRepository
                .findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(conversationId, pageable);

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

    private String generateConversationId(UUID userId) {
        return "conv-" + userId.toString();
    }

    private SupportMessageResponse toResponse(SupportMessage message) {
        return SupportMessageResponse.builder()
                .id(message.getId())
                .message(message.getMessage())
                .fromAdmin(message.isFromAdmin())
                .adminName(message.getAdmin() != null ? message.getAdmin().getName() : null)
                .read(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
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

