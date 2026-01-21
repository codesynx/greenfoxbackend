package com.greenfox.backend.modules.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Resort recommendation DTO for AI responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resort recommendation from AI")
public class ResortRecommendation {

    @Schema(description = "Resort unique identifier")
    private UUID id;

    @Schema(description = "Resort name")
    private String name;

    @Schema(description = "City location")
    private String city;

    @Schema(description = "Resort description")
    private String description;

    @Schema(description = "Base price per night in KZT")
    private BigDecimal basePrice;

    @Schema(description = "Rating (0-5)")
    private BigDecimal rating;

    @Schema(description = "Main photo URL")
    private String mainPhotoUrl;

    @Schema(description = "Available amenities")
    private List<String> amenities;

    @Schema(description = "Maximum guests capacity")
    private Integer maxGuests;
}
