package com.greenfox.backend.modules.support.repository;

import com.greenfox.backend.modules.support.entity.SupportMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for SupportMessage entity operations.
 */
@Repository
public interface SupportMessageRepository extends JpaRepository<SupportMessage, UUID> {

    /**
     * Find messages in a conversation.
     */
    Page<SupportMessage> findByConversationIdAndDeletedFalseOrderByCreatedAtAsc(
            String conversationId, Pageable pageable);

    /**
     * Find all messages for a user.
     */
    Page<SupportMessage> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Get distinct conversations with latest message (for admin).
     */
    @Query(value = """
            SELECT DISTINCT ON (conversation_id) * 
            FROM support_messages 
            WHERE is_deleted = false 
            ORDER BY conversation_id, created_at DESC
            """, nativeQuery = true)
    List<SupportMessage> findLatestMessagePerConversation();

    /**
     * Count unread messages from users (for admin).
     */
    @Query("SELECT COUNT(m) FROM SupportMessage m WHERE m.fromAdmin = false AND m.read = false AND m.deleted = false")
    long countUnreadFromUsers();

    /**
     * Count unread messages for a user.
     */
    long countByUserIdAndFromAdminTrueAndReadFalseAndDeletedFalse(UUID userId);

    /**
     * Mark all messages in conversation as read.
     */
    @Modifying
    @Query("UPDATE SupportMessage m SET m.read = true WHERE m.conversationId = :conversationId AND m.read = false")
    int markConversationAsRead(@Param("conversationId") String conversationId);

    /**
     * Mark user messages as read (for admin viewing).
     */
    @Modifying
    @Query("UPDATE SupportMessage m SET m.read = true " +
           "WHERE m.conversationId = :conversationId AND m.fromAdmin = false AND m.read = false")
    int markUserMessagesAsRead(@Param("conversationId") String conversationId);
}

