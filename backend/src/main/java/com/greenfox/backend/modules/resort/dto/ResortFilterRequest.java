package com.greenfox.backend.modules.resort.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for filtering resorts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resort filter criteria")
public class ResortFilterRequest {

    @Schema(description = "Filter by city")
    private String city;

    @Schema(description = "Minimum price")
    private BigDecimal minPrice;

    @Schema(description = "Maximum price")
    private BigDecimal maxPrice;

    @Schema(description = "Required amenities")
    private List<String> amenities;

    @Schema(description = "Check-in date for availability")
    private LocalDate checkIn;

    @Schema(description = "Check-out date for availability")
    private LocalDate checkOut;

    @Schema(description = "Minimum number of guests")
    private Integer guests;

    @Schema(description = "Search query (name/city)")
    private String query;

    @Schema(description = "User's latitude for distance sorting")
    private BigDecimal userLatitude;

    @Schema(description = "User's longitude for distance sorting")
    private BigDecimal userLongitude;
}

