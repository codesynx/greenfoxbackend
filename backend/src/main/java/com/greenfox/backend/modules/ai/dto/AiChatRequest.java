package com.greenfox.backend.modules.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for AI chat endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI Chat Request")
public class AiChatRequest {

    @NotBlank(message = "Message cannot be blank")
    @Schema(description = "User's message/query", example = "I need a hotel in Almaty with a pool")
    private String message;

    @NotBlank(message = "Language cannot be blank")
    @Pattern(regexp = "^(en|ru|kk)$", message = "Language must be 'en', 'ru', or 'kk'")
    @Schema(description = "App language code", example = "en", allowableValues = {"en", "ru", "kk"})
    private String language;
}
