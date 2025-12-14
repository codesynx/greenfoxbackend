package com.greenfox.backend.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for verifying OTP code.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to verify OTP code")
public class VerifyOtpRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{10,14}$", message = "Invalid phone number format")
    @Schema(description = "Phone number in international format", example = "+77001234567")
    private String phoneNumber;

    @NotBlank(message = "OTP code is required")
    @Size(min = 4, max = 6, message = "OTP code must be 4-6 digits")
    @Pattern(regexp = "^\\d+$", message = "OTP code must contain only digits")
    @Schema(description = "4-digit OTP code", example = "1234")
    private String code;
}

