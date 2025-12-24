package com.greenfox.backend.modules.booking.dto;

import com.greenfox.backend.modules.booking.entity.Booking;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for booking data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Booking information")
public class BookingResponse {

    @Schema(description = "Booking unique identifier")
    private UUID id;

    @Schema(description = "Booking unique identifier (alias for id)")
    private UUID bookingId;

    // Resort info
    @Schema(description = "Resort ID")
    private UUID resortId;

    @Schema(description = "Resort name")
    private String resortName;

    @Schema(description = "Resort city")
    private String resortCity;

    @Schema(description = "Resort main photo URL")
    private String resortPhotoUrl;

    // Dates
    @Schema(description = "Check-in date")
    private LocalDate checkInDate;

    @Schema(description = "Check-out date")
    private LocalDate checkOutDate;

    @Schema(description = "Number of nights")
    private Integer nights;

    // Guests
    @Schema(description = "Number of adults")
    private Integer adults;

    @Schema(description = "Number of children")
    private Integer children;

    @Schema(description = "Guest information")
    private Booking.GuestInfo guestInfo;

    // Pricing
    @Schema(description = "Base price (before discount)")
    private BigDecimal basePrice;

    @Schema(description = "Discount percentage")
    private Integer discountPercent;

    @Schema(description = "Discount amount")
    private BigDecimal discountAmount;

    @Schema(description = "Total price to pay")
    private BigDecimal totalPrice;

    // Status
    @Schema(description = "Booking status")
    private String status;

    // Payment
    @Schema(description = "Kaspi deep link for payment")
    private String kaspiDeepLink;

    @Schema(description = "Whether payment is required")
    private boolean paymentRequired;

    // Timestamps
    @Schema(description = "Booking creation time")
    private LocalDateTime createdAt;

    @Schema(description = "Payment confirmation time")
    private LocalDateTime paymentConfirmedAt;
}
