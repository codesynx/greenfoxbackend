package com.greenfox.backend.modules.promo.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.modules.promo.dto.PromoResponse;
import com.greenfox.backend.modules.promo.service.PromoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public promo endpoints for homepage banners.
 */
@RestController
@RequestMapping("/api/v1/promos")
@RequiredArgsConstructor
@Tag(name = "Promos", description = "Public promotional offers endpoints")
public class PromoController {

    private final PromoService promoService;

    @GetMapping
    @Operation(
            summary = "Get Active Promos",
            description = "Get list of currently active promotions for homepage banners"
    )
    public ResponseEntity<ApiResponse<List<PromoResponse>>> getActivePromos() {
        List<PromoResponse> promos = promoService.getActivePromos();
        return ResponseEntity.ok(ApiResponse.success(promos));
    }
}

