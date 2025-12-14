package com.greenfox.backend.modules.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating booking status (Admin only).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update booking status request")
public class UpdateBookingStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(CONFIRMED|COMPLETED|CANCELLED)$", 
             message = "Status must be CONFIRMED, COMPLETED, or CANCELLED")
    @Schema(description = "New booking status", example = "CONFIRMED")
    private String status;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Schema(description = "Admin notes")
    private String adminNotes;
}

