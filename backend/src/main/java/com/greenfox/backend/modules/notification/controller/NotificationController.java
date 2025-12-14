package com.greenfox.backend.modules.notification.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.modules.notification.dto.NotificationResponse;
import com.greenfox.backend.modules.notification.service.NotificationService;
import com.greenfox.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * User notification endpoints.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification endpoints")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(
            summary = "Get Notifications",
            description = "Get user's notifications with pagination"
    )
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<NotificationResponse> response = notificationService.getUserNotifications(principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    @Operation(
            summary = "Get Unread Count",
            description = "Get count of unread notifications"
    )
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        long count = notificationService.getUnreadCount(principal);
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PostMapping("/mark-all-read")
    @Operation(
            summary = "Mark All as Read",
            description = "Mark all notifications as read"
    )
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal);
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
    }
}

