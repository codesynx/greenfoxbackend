package com.greenfox.backend.modules.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending a support message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Send support message request")
public class SendMessageRequest {

    @NotBlank(message = "Message is required")
    @Size(min = 1, max = 5000, message = "Message must be between 1 and 5000 characters")
    @Schema(description = "Message content")
    private String message;
}

