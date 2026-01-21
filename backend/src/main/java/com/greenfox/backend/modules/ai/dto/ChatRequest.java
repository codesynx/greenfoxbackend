package com.greenfox.backend.modules.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for AI chat endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI Chat request")
public class ChatRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    @Schema(description = "User message to the AI assistant", example = "I'm looking for a resort near Alakol with a pool")
    private String message;

    @Schema(description = "Session ID for maintaining conversation memory. If not provided, a new session will be created.")
    private String sessionId;

    @Schema(description = "Previous conversation history for context")
    private List<ChatMessage> history;

    /**
     * Represents a single message in the conversation history.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Chat message in conversation history")
    public static class ChatMessage {

        @Schema(description = "Role of the message sender", example = "user", allowableValues = {"user", "assistant"})
        private String role;

        @Schema(description = "Content of the message")
        private String content;
    }
}
