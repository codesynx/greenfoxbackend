package com.greenfox.backend.modules.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for user ticket (conversation summary).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User support ticket")
public class TicketResponse {

    @Schema(description = "Ticket/conversation ID")
    private String ticketId;

    @Schema(description = "Message subject/title")
    private String subject;

    @Schema(description = "Message category")
    private String category;

    @Schema(description = "Last message preview")
    private String lastMessage;

    @Schema(description = "Number of unread messages")
    private int unreadCount;

    @Schema(description = "Last activity timestamp")
    private LocalDateTime lastActivityAt;

    @Schema(description = "Ticket creation timestamp")
    private LocalDateTime createdAt;
}

