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
 * Request DTO for creating a new resort (Admin only).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create resort request")
public class CreateResortRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    @Schema(description = "Resort name", example = "Shymbulak Mountain Resort")
    private String name;

    @NotNull(message = "Type is required")
    @Schema(description = "Resort type", example = "RESORT")
    private ResortType type;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    @Schema(description = "City location", example = "Almaty")
    private String city;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    @Schema(description = "Detailed description")
    private String description;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    @Schema(description = "Latitude coordinate", example = "43.1255")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    @Schema(description = "Longitude coordinate", example = "77.0774")
    private BigDecimal longitude;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Schema(description = "Full address")
    private String address;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01", message = "Base price must be greater than 0")
    @Schema(description = "Base price per night in KZT", example = "25000.00")
    private BigDecimal basePrice;

    @DecimalMin(value = "0.0", message = "Rating must be >= 0")
    @DecimalMax(value = "5.0", message = "Rating must be <= 5")
    @Schema(description = "Manual rating (0-5)", example = "4.5")
    private BigDecimal rating;

    @Min(value = 0, message = "Reviews count must be >= 0")
    @Schema(description = "Number of reviews (manual)", example = "127")
    private Integer reviewsCount;

    @Schema(description = "List of amenities", example = "[\"WiFi\", \"Pool\", \"Spa\", \"Restaurant\"]")
    private List<String> amenities;

    @Min(value = 1, message = "Max guests must be >= 1")
    @Schema(description = "Maximum number of guests", example = "6")
    private Integer maxGuests;
}

