package com.greenfox.backend.modules.ai.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.modules.ai.dto.ChatRequest;
import com.greenfox.backend.modules.ai.dto.ChatResponse;
import com.greenfox.backend.modules.ai.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI Chat endpoints for the Trips AI Mode feature.
 */
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "AI-powered travel concierge endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AiChatController {

    private final AiChatService aiChatService;

    /**
     * Chat with the AI travel concierge.
     * The AI can search for resorts based on user preferences and provide
     * personalized recommendations along with destination knowledge.
     *
     * @param request The chat request containing the user message
     * @return AI response with recommendations
     */
    @PostMapping("/chat")
    @Operation(
            summary = "Chat with AI Travel Concierge",
            description = "Send a message to the AI travel assistant and receive personalized " +
                    "resort recommendations. The AI can search for resorts based on your preferences " +
                    "and provides destination knowledge about Kazakhstan travel locations."
    )
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request) {

        ChatResponse response = aiChatService.chat(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
