package com.greenfox.backend.modules.support.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.modules.support.dto.SendMessageRequest;
import com.greenfox.backend.modules.support.dto.SupportMessageResponse;
import com.greenfox.backend.modules.support.service.SupportService;
import com.greenfox.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * User support chat endpoints.
 */
@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
@Tag(name = "Support", description = "User support chat endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SupportController {

    private final SupportService supportService;

    @PostMapping("/message")
    @Operation(
            summary = "Send Message (Creates New Ticket)",
            description = "Send a message to support. Each message creates a new ticket with a unique ID. " +
                         "Use subject and category to organize your tickets. " +
                         "Get the ticketId from the response or from /tickets endpoint to view conversation history."
    )
    public ResponseEntity<ApiResponse<SupportMessageResponse>> sendMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SendMessageRequest request) {
        SupportMessageResponse response = supportService.sendMessage(principal, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Message sent"));
    }

    @GetMapping("/history")
    @Operation(
            summary = "Get Chat History",
            description = "Get conversation history for a specific ticket. The ticketId is required and can be obtained from the /tickets endpoint."
    )
    public ResponseEntity<ApiResponse<PageResponse<SupportMessageResponse>>> getHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(
                    description = "Ticket/conversation ID (required). Get this from /tickets endpoint", 
                    required = true,
                    example = "conv-123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestParam(value = "ticketId") String ticketId,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC)
            Pageable pageable) {
        PageResponse<SupportMessageResponse> response = supportService.getHistory(principal, ticketId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tickets")
    @Operation(
            summary = "Get My Tickets",
            description = "Get list of all support tickets for the current user with subject, category, and last message"
    )
    public ResponseEntity<ApiResponse<java.util.List<com.greenfox.backend.modules.support.dto.TicketResponse>>> getMyTickets(
            @AuthenticationPrincipal UserPrincipal principal) {
        java.util.List<com.greenfox.backend.modules.support.dto.TicketResponse> tickets = supportService.getMyTickets(principal);
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }
}

