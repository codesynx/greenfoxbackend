package com.greenfox.backend.modules.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for calculating booking price.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Booking price calculation request")
public class BookingCalcRequest {

    @NotNull(message = "Resort ID is required")
    @Schema(description = "Resort ID")
    private UUID resortId;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    @Schema(description = "Check-in date")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    @Schema(description = "Check-out date")
    private LocalDate checkOutDate;

    @NotNull(message = "Number of adults is required")
    @Min(value = 1, message = "At least 1 adult is required")
    @Schema(description = "Number of adults", example = "2")
    private Integer adults;

    @Min(value = 0, message = "Children count cannot be negative")
    @Schema(description = "Number of children", example = "1")
    private Integer children;
}

