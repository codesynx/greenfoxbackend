package com.greenfox.backend.modules.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for conversation summary (for admin).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Conversation summary")
public class ConversationResponse {

    @Schema(description = "Conversation ID")
    private String conversationId;

    @Schema(description = "User ID")
    private UUID userId;

    @Schema(description = "User's phone number")
    private String userPhone;

    @Schema(description = "User's name")
    private String userName;

    @Schema(description = "Last message preview")
    private String lastMessage;

    @Schema(description = "Whether last message is from admin")
    private boolean lastFromAdmin;

    @Schema(description = "Number of unread messages from user")
    private int unreadCount;

    @Schema(description = "Last activity timestamp")
    private LocalDateTime lastActivityAt;
}

