package com.greenfox.backend.modules.resort.dto;

import com.greenfox.backend.modules.resort.entity.Resort;
import com.greenfox.backend.modules.resort.entity.ResortType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for resort data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resort information")
public class ResortResponse {

    @Schema(description = "Resort unique identifier")
    private UUID id;

    @Schema(description = "Resort name")
    private String name;

    @Schema(description = "Resort type")
    private ResortType type;

    @Schema(description = "City where resort is located")
    private String city;

    @Schema(description = "Detailed description")
    private String description;

    @Schema(description = "Latitude coordinate")
    private BigDecimal latitude;

    @Schema(description = "Longitude coordinate")
    private BigDecimal longitude;

    @Schema(description = "Full address")
    private String address;

    @Schema(description = "Base price per night")
    private BigDecimal basePrice;

    @Schema(description = "Average rating (0-5)")
    private BigDecimal rating;

    @Schema(description = "Number of reviews")
    private Integer reviewsCount;

    @Schema(description = "List of amenities")
    private List<String> amenities;

    @Schema(description = "Photos with descriptions")
    private List<Resort.ResortPhoto> photos;

    @Schema(description = "Maximum number of guests")
    private Integer maxGuests;

    @Schema(description = "Whether resort has active promotion")
    private boolean promo;

    @Schema(description = "Current promotional discount percentage (if promo)")
    private Integer promoDiscountPercent;

    @Schema(description = "Promotional price (if applicable)")
    private BigDecimal promoPrice;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}

