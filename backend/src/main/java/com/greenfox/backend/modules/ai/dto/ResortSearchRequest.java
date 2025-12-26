package com.greenfox.backend.modules.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for AI function calling - searchResorts tool parameters.
 * This class represents the parameters the AI can use when calling the searchResorts function.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Parameters for searching resorts via AI function calling")
public class ResortSearchRequest {

    @JsonProperty("city")
    @JsonPropertyDescription("City name to filter resorts (e.g., 'Almaty', 'Shymkent')")
    @Schema(description = "City name", example = "Almaty")
    private String city;

    @JsonProperty("amenities")
    @JsonPropertyDescription("List of required amenities (e.g., ['pool', 'wifi', 'parking'])")
    @Schema(description = "Required amenities", example = "[\"pool\", \"wifi\"]")
    private List<String> amenities;

    @JsonProperty("maxPrice")
    @JsonPropertyDescription("Maximum price per night in KZT")
    @Schema(description = "Maximum price per night", example = "50000")
    private BigDecimal maxPrice;

    @JsonProperty("minPrice")
    @JsonPropertyDescription("Minimum price per night in KZT")
    @Schema(description = "Minimum price per night", example = "10000")
    private BigDecimal minPrice;

    @JsonProperty("guests")
    @JsonPropertyDescription("Minimum number of guests the resort should accommodate")
    @Schema(description = "Number of guests", example = "4")
    private Integer guests;

    @JsonProperty("query")
    @JsonPropertyDescription("General search query for name or description")
    @Schema(description = "Search query", example = "luxury resort")
    private String query;
}
