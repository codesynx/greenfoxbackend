package com.greenfox.backend.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for user profile data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User profile information")
public class UserProfileResponse {

    @Schema(description = "User's unique identifier")
    private UUID id;

    @Schema(description = "User's phone number")
    private String phoneNumber;

    @Schema(description = "User's display name")
    private String name;

    @Schema(description = "URL to user's avatar image")
    private String avatarUrl;

    @Schema(description = "User's role (ADMIN/USER)")
    private String role;

    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;
}

