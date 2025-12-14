package com.greenfox.backend.modules.notification.entity;

import com.greenfox.backend.common.entity.BaseEntity;
import com.greenfox.backend.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Notification entity for user notifications.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_user", columnList = "user_id"),
        @Index(name = "idx_notifications_read", columnList = "is_read")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    /**
     * Notification types.
     */
    public enum NotificationType {
        BOOKING_CREATED,
        BOOKING_PAID,
        BOOKING_CONFIRMED,
        BOOKING_COMPLETED,
        BOOKING_CANCELLED,
        PROMO_NEW,
        SYSTEM
    }
}

