package com.greenfox.backend.modules.resort.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for favorite resort.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Favorite resort response")
public class FavoriteResponse {

    @Schema(description = "Favorite ID")
    private UUID id;

    @Schema(description = "Resort details")
    private ResortListResponse resort;

    @Schema(description = "When the resort was added to favorites")
    private LocalDateTime favoritedAt;
}

