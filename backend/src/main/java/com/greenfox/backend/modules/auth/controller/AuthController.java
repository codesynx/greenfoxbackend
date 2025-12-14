package com.greenfox.backend.modules.auth.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.modules.auth.dto.*;
import com.greenfox.backend.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller for OTP-based login flow.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "OTP-based authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-otp")
    @Operation(
            summary = "Send OTP",
            description = "Send a 4-digit OTP code to the specified phone number for authentication"
    )
    public ResponseEntity<ApiResponse<OtpResponse>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {
        OtpResponse response = authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.success(response, "OTP sent successfully"));
    }

    @PostMapping("/resend-otp")
    @Operation(
            summary = "Resend OTP",
            description = "Resend OTP code with throttling (1 minute between requests)"
    )
    public ResponseEntity<ApiResponse<OtpResponse>> resendOtp(
            @Valid @RequestBody SendOtpRequest request) {
        OtpResponse response = authService.resendOtp(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/verify-otp")
    @Operation(
            summary = "Verify OTP",
            description = "Verify OTP code and authenticate user. Creates new user if not exists."
    )
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        String message = response.isNewUser() ? "Registration successful" : "Login successful";
        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh Token",
            description = "Get new access and refresh tokens using a valid refresh token"
    )
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }
}

