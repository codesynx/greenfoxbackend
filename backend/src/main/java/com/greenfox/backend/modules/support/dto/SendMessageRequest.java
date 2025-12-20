package com.greenfox.backend.modules.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending a support message.
 * Each message creates a new ticket with a unique conversation ID.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Send support message request. Each message creates a new ticket.")
public class SendMessageRequest {

    @Size(max = 200, message = "Subject must not exceed 200 characters")
    @Schema(description = "Message subject/title", example = "Payment Issue")
    private String subject;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    @Schema(description = "Message category", example = "PAYMENT", 
            allowableValues = {"PAYMENT", "BOOKING", "TECHNICAL", "REFUND", "OTHER"})
    private String category;

    @NotBlank(message = "Message is required")
    @Size(min = 1, max = 5000, message = "Message must be between 1 and 5000 characters")
    @Schema(description = "Message content")
    private String message;
}

