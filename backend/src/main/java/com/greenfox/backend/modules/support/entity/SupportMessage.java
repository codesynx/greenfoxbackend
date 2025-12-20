package com.greenfox.backend.modules.support.entity;

import com.greenfox.backend.common.entity.BaseEntity;
import com.greenfox.backend.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Support message entity for chat/ticket system.
 */
@Entity
@Table(name = "support_messages", indexes = {
        @Index(name = "idx_support_user", columnList = "user_id"),
        @Index(name = "idx_support_conversation", columnList = "conversation_id"),
        @Index(name = "idx_support_category", columnList = "category")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    @Column(name = "subject", length = 200)
    private String subject;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_from_admin", nullable = false)
    @Builder.Default
    private boolean fromAdmin = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;
}

