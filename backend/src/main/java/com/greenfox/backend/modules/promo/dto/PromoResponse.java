package com.greenfox.backend.modules.promo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for promo data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Promotional offer information")
public class PromoResponse {

    @Schema(description = "Promo unique identifier")
    private UUID id;

    @Schema(description = "Resort ID this promo applies to")
    private UUID resortId;

    @Schema(description = "Resort name")
    private String resortName;

    @Schema(description = "Resort city")
    private String resortCity;

    @Schema(description = "Discount percentage")
    private Integer discountPercent;

    @Schema(description = "Promo start date")
    private LocalDate startDate;

    @Schema(description = "Promo end date")
    private LocalDate endDate;

    @Schema(description = "Banner image URL")
    private String bannerImageUrl;

    @Schema(description = "Promo title")
    private String title;

    @Schema(description = "Promo description")
    private String description;

    @Schema(description = "Original price")
    private BigDecimal originalPrice;

    @Schema(description = "Discounted price")
    private BigDecimal promoPrice;

    @Schema(description = "Whether promo is currently active")
    private boolean active;
}

