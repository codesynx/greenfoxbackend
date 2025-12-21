package com.greenfox.backend.modules.promo.dto;

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
@Schema(description = "Create promotion request")
public class CreatePromoRequest {

    @NotNull(message = "Resort ID is required")
    @Schema(description = "Resort ID to apply promotion to")
    private UUID resortId;

    @NotNull(message = "Discount percent is required")
    @Min(value = 1, message = "Discount must be at least 1%")
    @Max(value = 99, message = "Discount cannot exceed 99%")
    @Schema(description = "Discount percentage", example = "15")
    private Integer discountPercent;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    @Schema(description = "Promotion start date")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    @Schema(description = "Promotion end date")
    private LocalDate endDate;

    @Size(max = 500, message = "Banner URL must not exceed 500 characters")
    @Schema(description = "Banner image URL (optional - can be uploaded separately via /{id}/banner/upload endpoint)")
    private String bannerImageUrl;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Schema(description = "Promo title", example = "Winter Special")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Promo description")
    private String description;
}

