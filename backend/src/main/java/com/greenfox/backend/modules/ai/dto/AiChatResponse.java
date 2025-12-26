package com.greenfox.backend.modules.ai.dto;

import com.greenfox.backend.modules.resort.dto.ResortListResponse;
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
@Schema(description = "AI Chat Response")
public class AiChatResponse {

    @Schema(description = "AI's conversational reply to the user")
    private String replyText;

    @Schema(description = "List of resort recommendations (if any were found via searchResorts tool)")
    private List<ResortListResponse> recommendations;
}
