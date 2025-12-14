package com.greenfox.backend.modules.promo.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.modules.promo.dto.CreatePromoRequest;
import com.greenfox.backend.modules.promo.dto.PromoResponse;
import com.greenfox.backend.modules.promo.service.PromoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin-only promo management endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin/promos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Promos", description = "Promotion management endpoints (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminPromoController {

    private final PromoService promoService;

    @GetMapping
    @Operation(
            summary = "List All Promos",
            description = "Get all promotions (active and inactive)"
    )
    public ResponseEntity<ApiResponse<PageResponse<PromoResponse>>> getAllPromos(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<PromoResponse> promos = promoService.getAllPromos(pageable);
        return ResponseEntity.ok(ApiResponse.success(promos));
    }

    @PostMapping
    @Operation(
            summary = "Create Promo",
            description = "Create a new promotional offer for a resort"
    )
    public ResponseEntity<ApiResponse<PromoResponse>> createPromo(
            @Valid @RequestBody CreatePromoRequest request) {
        PromoResponse promo = promoService.createPromo(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(promo, "Promotion created successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete Promo",
            description = "Delete a promotional offer"
    )
    public ResponseEntity<ApiResponse<Void>> deletePromo(@PathVariable UUID id) {
        promoService.deletePromo(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Promotion deleted successfully"));
    }
}

