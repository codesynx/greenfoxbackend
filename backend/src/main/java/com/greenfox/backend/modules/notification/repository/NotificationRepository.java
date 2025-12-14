package com.greenfox.backend.modules.notification.repository;

import com.greenfox.backend.modules.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for Notification entity operations.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Find notifications for a user.
     */
    Page<Notification> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Count unread notifications for a user.
     */
    long countByUserIdAndReadFalseAndDeletedFalse(UUID userId);

    /**
     * Mark all notifications as read for a user.
     */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId AND n.read = false")
    int markAllAsRead(@Param("userId") UUID userId);

    /**
     * Find notifications for admins (for admin panel notifications).
     */
    @Query("SELECT n FROM Notification n WHERE n.user.role = 'ADMIN' AND n.deleted = false " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findAdminNotifications(Pageable pageable);
}

