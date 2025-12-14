package com.greenfox.backend.modules.support.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.modules.support.dto.SendMessageRequest;
import com.greenfox.backend.modules.support.dto.SupportMessageResponse;
import com.greenfox.backend.modules.support.service.SupportService;
import com.greenfox.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * User support chat endpoints.
 */
@RestController
@RequestMapping("/api/v1/support")
@RequiredArgsConstructor
@Tag(name = "Support", description = "User support chat endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SupportController {

    private final SupportService supportService;

    @PostMapping("/message")
    @Operation(
            summary = "Send Message",
            description = "Send a message to support"
    )
    public ResponseEntity<ApiResponse<SupportMessageResponse>> sendMessage(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SendMessageRequest request) {
        SupportMessageResponse response = supportService.sendMessage(principal, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Message sent"));
    }

    @GetMapping("/history")
    @Operation(
            summary = "Get Chat History",
            description = "Get conversation history with support"
    )
    public ResponseEntity<ApiResponse<PageResponse<SupportMessageResponse>>> getHistory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.ASC)
            Pageable pageable) {
        PageResponse<SupportMessageResponse> response = supportService.getHistory(principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

