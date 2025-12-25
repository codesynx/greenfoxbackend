package com.greenfox.backend.modules.notification.service;

import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.modules.booking.entity.Booking;
import com.greenfox.backend.modules.notification.dto.NotificationResponse;
import com.greenfox.backend.modules.notification.entity.Notification;
import com.greenfox.backend.modules.notification.entity.Notification.NotificationType;
import com.greenfox.backend.modules.notification.repository.NotificationRepository;
import com.greenfox.backend.modules.user.entity.User;
import com.greenfox.backend.modules.user.repository.UserRepository;
import com.greenfox.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for notification management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Get user's notifications.
     */
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getUserNotifications(UserPrincipal principal, Pageable pageable) {
        Page<Notification> notifications = notificationRepository
                .findByUserIdAndDeletedFalseOrderByCreatedAtDesc(principal.getId(), pageable);

        List<NotificationResponse> content = notifications.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.from(notifications, content);
    }

    /**
     * Get unread notification count.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(UserPrincipal principal) {
        return notificationRepository.countByUserIdAndReadFalseAndDeletedFalse(principal.getId());
    }

    /**
     * Mark all notifications as read.
     */
    @Transactional
    public void markAllAsRead(UserPrincipal principal) {
        int updated = notificationRepository.markAllAsRead(principal.getId());
        log.info("Marked {} notifications as read for user: {}", updated, principal.getId());
    }

    /**
     * Create a booking notification for a user.
     */
    @Transactional
    public void createBookingNotification(Booking booking, String message) {
        NotificationType type = switch (booking.getStatus()) {
            case PENDING -> NotificationType.BOOKING_CREATED;
            case PAID_WAITING -> NotificationType.BOOKING_PAID;
            case CONFIRMED -> NotificationType.BOOKING_CONFIRMED;
            case REJECTED -> NotificationType.BOOKING_REJECTED;
            case CANCELLATION_PENDING -> NotificationType.CANCELLATION_UNDER_REVIEW;
            case COMPLETED -> NotificationType.BOOKING_COMPLETED;
            case CANCELLED -> NotificationType.BOOKING_CANCELLED;
        };

        Notification notification = Notification.builder()
                .user(booking.getUser())
                .type(type)
                .title("Booking Update: " + booking.getResort().getName())
                .message(message)
                .referenceId(booking.getId())
                .referenceType("BOOKING")
                .build();

        notificationRepository.save(notification);
        log.info("Notification created for user: {} type: {}", booking.getUser().getId(), type);

        // TODO: Send push notification via FCM if user has device token
        sendPushNotification(booking.getUser(), notification);
    }

    /**
     * Create admin notification (for all admins).
     */
    @Transactional
    public void createAdminNotification(String message, UUID referenceId) {
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.UserRole.ADMIN && !u.isDeleted())
                .toList();

        for (User admin : admins) {
            Notification notification = Notification.builder()
                    .user(admin)
                    .type(NotificationType.SYSTEM)
                    .title("Admin Alert")
                    .message(message)
                    .referenceId(referenceId)
                    .referenceType("BOOKING")
                    .build();

            notificationRepository.save(notification);
        }

        log.info("Admin notification created for {} admins", admins.size());
    }

    private void sendPushNotification(User user, Notification notification) {
        if (user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            return;
        }

        // TODO: Implement Firebase Cloud Messaging integration
        log.info("Push notification would be sent to user: {} with token: {}...", 
                user.getId(), user.getFcmToken().substring(0, 10));
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
