package com.greenfox.backend.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for successful authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response with tokens")
public class AuthResponse {

    @Schema(description = "User ID")
    private UUID userId;

    @Schema(description = "User's phone number")
    private String phoneNumber;

    @Schema(description = "User's name")
    private String name;

    @Schema(description = "User's role (ADMIN/USER)")
    private String role;

    @Schema(description = "Whether this is a new user registration")
    private boolean newUser;

    @Schema(description = "JWT access token for API requests")
    private String accessToken;

    @Schema(description = "JWT refresh token for obtaining new access tokens")
    private String refreshToken;

    @Schema(description = "Access token expiration time in milliseconds")
    private long expiresIn;
}

