package com.greenfox.backend.modules.resort.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Compact response DTO for resort listings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resort list item (compact)")
public class ResortListResponse {

    @Schema(description = "Resort unique identifier")
    private UUID id;

    @Schema(description = "Resort name")
    private String name;

    @Schema(description = "City where resort is located")
    private String city;

    @Schema(description = "Base price per night")
    private BigDecimal basePrice;

    @Schema(description = "Average rating (0-5)")
    private BigDecimal rating;

    @Schema(description = "Number of reviews")
    private Integer reviewsCount;

    @Schema(description = "Main photo URL")
    private String mainPhotoUrl;

    @Schema(description = "Whether resort has active promotion")
    private boolean promo;

    @Schema(description = "Promotional price (if applicable)")
    private BigDecimal promoPrice;

    @Schema(description = "Maximum number of guests")
    private Integer maxGuests;
}

