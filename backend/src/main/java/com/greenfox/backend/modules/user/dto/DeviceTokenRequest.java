package com.greenfox.backend.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for saving FCM device token for push notifications.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Device token registration for push notifications")
public class DeviceTokenRequest {

    @NotBlank(message = "Device token is required")
    @Schema(description = "Firebase Cloud Messaging device token")
    private String deviceToken;
}

