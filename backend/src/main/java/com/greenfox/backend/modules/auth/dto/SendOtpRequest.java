package com.greenfox.backend.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending OTP to a phone number.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to send OTP to phone number")
public class SendOtpRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{10,14}$", message = "Invalid phone number format")
    @Schema(description = "Phone number in international format", example = "+77001234567")
    private String phoneNumber;
}

