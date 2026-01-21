package com.greenfox.backend.modules.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for AI chat endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI Chat response with recommendations")
public class ChatResponse {

    @Schema(description = "AI assistant reply text")
    private String replyText;

    @Schema(description = "Session ID for the conversation")
    private String sessionId;

    @Schema(description = "Resort recommendations found via search (if any)")
    private List<ResortRecommendation> recommendations;

    @Schema(description = "Whether a resort search was performed")
    private boolean searchPerformed;
}
