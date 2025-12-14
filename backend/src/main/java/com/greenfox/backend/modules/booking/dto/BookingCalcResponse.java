package com.greenfox.backend.modules.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for booking price calculation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Booking price calculation result")
public class BookingCalcResponse {

    @Schema(description = "Resort ID")
    private UUID resortId;

    @Schema(description = "Resort name")
    private String resortName;

    @Schema(description = "Check-in date")
    private LocalDate checkInDate;

    @Schema(description = "Check-out date")
    private LocalDate checkOutDate;

    @Schema(description = "Number of nights")
    private Integer nights;

    @Schema(description = "Number of adults")
    private Integer adults;

    @Schema(description = "Number of children")
    private Integer children;

    @Schema(description = "Base price per night")
    private BigDecimal basePricePerNight;

    @Schema(description = "Base total (before discount)")
    private BigDecimal baseTotal;

    @Schema(description = "Discount percentage (if promo)")
    private Integer discountPercent;

    @Schema(description = "Discount amount")
    private BigDecimal discountAmount;

    @Schema(description = "Final total price")
    private BigDecimal totalPrice;

    @Schema(description = "Whether resort has active promo")
    private boolean hasPromo;

    @Schema(description = "Whether dates are available")
    private boolean available;

    @Schema(description = "Unavailability reason if not available")
    private String unavailableReason;
}

