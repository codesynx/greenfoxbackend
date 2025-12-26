package com.greenfox.backend.modules.ai.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.modules.ai.dto.AiChatRequest;
import com.greenfox.backend.modules.ai.dto.AiChatResponse;
import com.greenfox.backend.modules.ai.service.TripAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for AI-powered trip planning and resort recommendations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Trip Assistant", description = "AI-powered travel assistant using Google Gemini")
public class TripAiController {

    private final TripAssistantService tripAssistantService;

    @PostMapping("/chat")
    @Operation(
            summary = "Chat with AI Trip Assistant",
            description = "Send a message to the AI travel assistant and get personalized resort recommendations. " +
                    "The AI uses function calling to search the database for real resort data. " +
                    "Supports English (en), Russian (ru), and Kazakh (kk) languages."
    )
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(@Valid @RequestBody AiChatRequest request) {
        log.info("AI chat request received - Language: {}", request.getLanguage());

        AiChatResponse response = tripAssistantService.chat(
                request.getMessage(),
                request.getLanguage()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
