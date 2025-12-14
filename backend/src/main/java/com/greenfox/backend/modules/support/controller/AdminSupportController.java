package com.greenfox.backend.modules.support.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.modules.support.dto.ConversationResponse;
import com.greenfox.backend.modules.support.dto.SendMessageRequest;
import com.greenfox.backend.modules.support.dto.SupportMessageResponse;
import com.greenfox.backend.modules.support.service.SupportService;
import com.greenfox.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin support management endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin/support")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Support", description = "Support management endpoints (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminSupportController {

    private final SupportService supportService;

    @GetMapping("/conversations")
    @Operation(
            summary = "Get All Conversations",
            description = "Get list of all support conversations/tickets"
    )
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversations() {
        List<ConversationResponse> response = supportService.getConversations();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/conversations/{userId}")
    @Operation(
            summary = "Get Conversation",
            description = "Get messages for a specific user conversation"
    )
    public ResponseEntity<ApiResponse<PageResponse<SupportMessageResponse>>> getConversation(
            @PathVariable UUID userId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC)
            Pageable pageable) {
        PageResponse<SupportMessageResponse> response = supportService.getConversation(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{userId}/reply")
    @Operation(
            summary = "Reply to User",
            description = "Send a reply to a user's support conversation"
    )
    public ResponseEntity<ApiResponse<SupportMessageResponse>> replyToUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SendMessageRequest request) {
        SupportMessageResponse response = supportService.replyToUser(userId, principal, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Reply sent"));
    }

    @GetMapping("/unread-count")
    @Operation(
            summary = "Get Unread Count",
            description = "Get count of unread messages from users"
    )
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        long count = supportService.getUnreadCount();
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }
}

