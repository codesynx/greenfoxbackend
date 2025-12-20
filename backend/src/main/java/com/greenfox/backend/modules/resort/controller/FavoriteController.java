package com.greenfox.backend.modules.resort.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.modules.resort.dto.FavoriteResponse;
import com.greenfox.backend.modules.resort.service.FavoriteService;
import com.greenfox.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Favorite resorts controller.
 */
@RestController
@RequestMapping("/api/v1/resorts")
@RequiredArgsConstructor
@Tag(name = "Favorites", description = "Favorite resorts management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{resortId}/favorite")
    @Operation(
            summary = "Add to Favorites",
            description = "Add a resort to the current user's favorites"
    )
    public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Resort UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID resortId) {
        FavoriteResponse response = favoriteService.addFavorite(principal, resortId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Resort added to favorites"));
    }

    @DeleteMapping("/{resortId}/favorite")
    @Operation(
            summary = "Remove from Favorites",
            description = "Remove a resort from the current user's favorites"
    )
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Resort UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID resortId) {
        favoriteService.removeFavorite(principal, resortId);
        return ResponseEntity.ok(ApiResponse.success(null, "Resort removed from favorites"));
    }

    @GetMapping("/favorites")
    @Operation(
            summary = "Get My Favorites",
            description = "Get paginated list of favorite resorts for the current user"
    )
    public ResponseEntity<ApiResponse<PageResponse<FavoriteResponse>>> getMyFavorites(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<FavoriteResponse> favorites = favoriteService.getMyFavorites(principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    @GetMapping("/{resortId}/favorite/status")
    @Operation(
            summary = "Check Favorite Status",
            description = "Check if a resort is in the current user's favorites"
    )
    public ResponseEntity<ApiResponse<Boolean>> isFavorited(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "Resort UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID resortId) {
        boolean isFavorited = favoriteService.isFavorited(principal, resortId);
        return ResponseEntity.ok(ApiResponse.success(isFavorited));
    }
}

