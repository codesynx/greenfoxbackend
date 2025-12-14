package com.greenfox.backend.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for OTP send/resend operations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OTP operation response")
public class OtpResponse {

    @Schema(description = "Phone number the OTP was sent to")
    private String phoneNumber;

    @Schema(description = "OTP expiration time in seconds")
    private int expiresInSeconds;

    @Schema(description = "Time until next OTP can be requested (throttling)")
    private int retryAfterSeconds;

    @Schema(description = "Message about the operation")
    private String message;
}

