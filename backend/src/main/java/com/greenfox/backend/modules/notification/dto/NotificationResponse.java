package com.greenfox.backend.modules.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for notification data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Notification information")
public class NotificationResponse {

    @Schema(description = "Notification unique identifier")
    private UUID id;

    @Schema(description = "Notification type")
    private String type;

    @Schema(description = "Notification title")
    private String title;

    @Schema(description = "Notification message")
    private String message;

    @Schema(description = "Reference ID (e.g., booking ID)")
    private UUID referenceId;

    @Schema(description = "Reference type (e.g., BOOKING)")
    private String referenceType;

    @Schema(description = "Whether notification has been read")
    private boolean read;

    @Schema(description = "Notification creation time")
    private LocalDateTime createdAt;
}

