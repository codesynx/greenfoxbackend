package com.greenfox.backend.modules.ai.controller;

import com.greenfox.backend.modules.ai.dto.AiChatRequest;
import com.greenfox.backend.modules.ai.dto.AiChatResponse;
import com.greenfox.backend.modules.ai.service.TripAssistantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Trip Assistant", description = "AI-powered travel concierge")
public class AiController {

    private final TripAssistantService tripAssistantService;

    @PostMapping("/chat")
    @Operation(summary = "Chat with the AI Trip Assistant")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = tripAssistantService.chat(request.getMessage());
        return ResponseEntity.ok(response);
    }
}
