package com.greenfox.backend.modules.resort.dto;

import com.greenfox.backend.modules.resort.entity.ResortType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for updating an existing resort (Admin only).
 * All fields are optional - only provided fields will be updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update resort request")
public class UpdateResortRequest {

    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    @Schema(description = "Resort name")
    private String name;

    @Schema(description = "Resort type")
    private ResortType type;

    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "City location")
    private String city;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @Schema(description = "Detailed description")
    private String description;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    @Schema(description = "Latitude coordinate")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    @Schema(description = "Longitude coordinate")
    private BigDecimal longitude;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Schema(description = "Full address")
    private String address;

    @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
    @Schema(description = "Base price per night in KZT")
    private BigDecimal basePrice;

    @DecimalMin(value = "0.0", message = "Rating must be >= 0")
    @DecimalMax(value = "5.0", message = "Rating must be <= 5")
    @Schema(description = "Manual rating (0-5)")
    private BigDecimal rating;

    @Min(value = 0, message = "Reviews count must be >= 0")
    @Schema(description = "Number of reviews (manual)")
    private Integer reviewsCount;

    @Schema(description = "List of amenities")
    private List<String> amenities;

    @Min(value = 1, message = "Max guests must be >= 1")
    @Schema(description = "Maximum number of guests")
    private Integer maxGuests;

    @Schema(description = "Whether resort is active")
    private Boolean active;
}

