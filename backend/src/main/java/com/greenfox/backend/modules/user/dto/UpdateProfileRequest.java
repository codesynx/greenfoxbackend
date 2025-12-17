package com.greenfox.backend.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user profile.
 * Note: Phone number cannot be changed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profile update request")
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Schema(description = "User's display name", example = "Aibek Nurlan")
    private String name;

    @jakarta.validation.constraints.Email(message = "Invalid email format")
    @Schema(description = "User's email address", example = "aibek@example.kz")
    private String email;

    @Schema(description = "URL to user's avatar image (or base64 for upload)")
    private String avatarUrl;
}

