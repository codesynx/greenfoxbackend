package com.greenfox.backend.modules.promo.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.modules.promo.dto.CreatePromoRequest;
import com.greenfox.backend.modules.promo.dto.PromoResponse;
import com.greenfox.backend.modules.promo.service.PromoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create Promo",
            description = "Create a new promotional offer for a resort"
    )
    public ResponseEntity<ApiResponse<PromoResponse>> createPromo(
            @Valid @RequestBody CreatePromoRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        // Log incoming request details
        System.out.println("=== CREATE PROMO ENDPOINT HIT ===");
        System.out.println("Content-Type: " + httpRequest.getContentType());
        System.out.println("Request Object: " + request);
        System.out.println("ResortId: " + request.getResortId());
        System.out.println("DiscountPercent: " + request.getDiscountPercent());
        System.out.println("StartDate: " + request.getStartDate());
        System.out.println("EndDate: " + request.getEndDate());
        System.out.println("================================");

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

    @PostMapping(value = "/{id}/banner/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Upload Promo Banner",
            description = "Upload a banner image for a promo using multipart/form-data. " +
                         "The file will be automatically uploaded to GCP Cloud Storage and set as the promo's banner. " +
                         "Supported formats: JPEG, PNG, WebP. Maximum file size: 10MB. " +
                         "In Swagger UI, click 'Try it out' and use the 'Choose File' button to select an image."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Multipart form data with banner image file",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(type = "object", description = "Form data with image file")
            )
    )
    public ResponseEntity<ApiResponse<PromoResponse>> uploadBanner(
            @Parameter(description = "Promo UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(
                    description = "Banner image file to upload (JPEG, PNG, or WebP). In Swagger UI, use the file picker button.",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestParam("file") MultipartFile file) {
        try {
            PromoResponse promo = promoService.uploadBanner(id, file);
            return ResponseEntity.ok(ApiResponse.success(promo, "Banner uploaded successfully"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("UPLOAD_ERROR", "Failed to upload banner: " + e.getMessage()));
        }
    }
}
