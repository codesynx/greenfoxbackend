package com.greenfox.backend.modules.promo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating a promotion (Admin only).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Create promotion request")
public class CreatePromoRequest {

    @JsonProperty("resortId")
    @NotNull(message = "Resort ID is required")
    @Schema(description = "Resort ID to apply promotion to")
    private UUID resortId;

    @JsonProperty("discountPercent")
    @NotNull(message = "Discount percent is required")
    @Min(value = 1, message = "Discount must be at least 1%")
    @Max(value = 99, message = "Discount cannot exceed 99%")
    @Schema(description = "Discount percentage", example = "15")
    private Integer discountPercent;

    @JsonProperty("startDate")
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Promotion start date")
    private LocalDate startDate;

    @JsonProperty("endDate")
    @NotNull(message = "End date is required")
    @FutureOrPresent(message = "End date must be today or in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Promotion end date")
    private LocalDate endDate;

    @JsonProperty("bannerImageUrl")
    @Size(max = 500, message = "Banner URL must not exceed 500 characters")
    @Schema(description = "Banner image URL (optional - can be uploaded separately via /{id}/banner/upload endpoint)")
    private String bannerImageUrl;

    @JsonProperty("title")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(description = "Promo title", example = "Winter Special")
    private String title;

    @JsonProperty("description")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Promo description")
    private String description;
}
