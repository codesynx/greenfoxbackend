package com.greenfox.backend.modules.resort.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.modules.resort.dto.*;
import com.greenfox.backend.modules.resort.service.ResortService;
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

import java.util.List;
import java.util.UUID;

/**
 * Admin-only resort management endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin/resorts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Resorts", description = "Resort management endpoints (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminResortController {

    private final ResortService resortService;

    @GetMapping
    @Operation(
            summary = "List All Resorts (Admin)",
            description = "Get all resorts including inactive ones"
    )
    public ResponseEntity<ApiResponse<PageResponse<ResortResponse>>> getAllResorts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) 
            Pageable pageable) {
        PageResponse<ResortResponse> resorts = resortService.getResortsForAdmin(pageable);
        return ResponseEntity.ok(ApiResponse.success(resorts));
    }

    @PostMapping
    @Operation(
            summary = "Create Resort",
            description = "Create a new resort"
    )
    public ResponseEntity<ApiResponse<ResortResponse>> createResort(
            @Valid @RequestBody CreateResortRequest request) {
        ResortResponse resort = resortService.createResort(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(resort, "Resort created successfully"));
    }

    @PatchMapping("/{id}")
    @Operation(
            summary = "Update Resort",
            description = "Update an existing resort"
    )
    public ResponseEntity<ApiResponse<ResortResponse>> updateResort(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateResortRequest request) {
        ResortResponse resort = resortService.updateResort(id, request);
        return ResponseEntity.ok(ApiResponse.success(resort, "Resort updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete Resort",
            description = "Soft delete a resort"
    )
    public ResponseEntity<ApiResponse<Void>> deleteResort(@PathVariable UUID id) {
        resortService.deleteResort(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Resort deleted successfully"));
    }

    @PostMapping("/{id}/photos")
    @Operation(
            summary = "Add Photos",
            description = "Add photos to a resort (max 15 total)"
    )
    public ResponseEntity<ApiResponse<ResortResponse>> addPhotos(
            @PathVariable UUID id,
            @Valid @RequestBody List<ResortPhotoRequest> photos) {
        ResortResponse resort = resortService.addPhotos(id, photos);
        return ResponseEntity.ok(ApiResponse.success(resort, "Photos added successfully"));
    }

    @DeleteMapping("/{id}/photos/{order}")
    @Operation(
            summary = "Remove Photo",
            description = "Remove a photo from a resort by its order index"
    )
    public ResponseEntity<ApiResponse<ResortResponse>> removePhoto(
            @PathVariable UUID id,
            @PathVariable int order) {
        ResortResponse resort = resortService.removePhoto(id, order);
        return ResponseEntity.ok(ApiResponse.success(resort, "Photo removed successfully"));
    }
}

