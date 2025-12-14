package com.greenfox.backend.modules.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for support message.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Support message")
public class SupportMessageResponse {

    @Schema(description = "Message unique identifier")
    private UUID id;

    @Schema(description = "Message content")
    private String message;

    @Schema(description = "Whether message is from admin")
    private boolean fromAdmin;

    @Schema(description = "Admin name if from admin")
    private String adminName;

    @Schema(description = "Whether message has been read")
    private boolean read;

    @Schema(description = "Message timestamp")
    private LocalDateTime createdAt;
}

