package com.greenfox.backend.modules.resort.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.modules.resort.dto.*;
import com.greenfox.backend.modules.resort.service.ResortService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Create Resort with Photos",
            description = "Create a new resort with optional photos. All photos will be uploaded to GCP Cloud Storage. " +
                         "Supported formats: JPEG, PNG, WebP. Maximum file size: 10MB per photo. Maximum 15 photos. " +
                         "In Swagger UI, click 'Try it out' and use the file picker buttons to select images."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Multipart form data with resort details and photos",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(type = "object", description = "Form data with resort fields and photo files")
            )
    )
    public ResponseEntity<ApiResponse<ResortResponse>> createResort(
            @Parameter(description = "Resort name", required = true)
            @RequestParam("name") String name,
            @Parameter(description = "City location", required = true)
            @RequestParam("city") String city,
            @Parameter(description = "Base price per night in KZT", required = true)
            @RequestParam("basePrice") String basePrice,
            @Parameter(description = "Detailed description")
            @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Latitude coordinate")
            @RequestParam(value = "latitude", required = false) String latitude,
            @Parameter(description = "Longitude coordinate")
            @RequestParam(value = "longitude", required = false) String longitude,
            @Parameter(description = "Full address")
            @RequestParam(value = "address", required = false) String address,
            @Parameter(description = "Manual rating (0-5)")
            @RequestParam(value = "rating", required = false) String rating,
            @Parameter(description = "Number of reviews (manual)")
            @RequestParam(value = "reviewsCount", required = false) String reviewsCount,
            @Parameter(description = "Maximum number of guests")
            @RequestParam(value = "maxGuests", required = false) String maxGuests,
            @Parameter(description = "Comma-separated list of amenities (e.g., 'WiFi,Pool,Spa')")
            @RequestParam(value = "amenities", required = false) String amenities,
            @Parameter(
                    description = "Photo files to upload (JPEG, PNG, or WebP). In Swagger UI, use the file picker buttons.",
                    required = false
            )
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            @Parameter(description = "Photo descriptions (comma-separated, matching order of photos)")
            @RequestParam(value = "photoDescriptions", required = false) String photoDescriptions) {
        try {
            ResortResponse resort = resortService.createResortWithPhotos(
                    name, city, basePrice, description, latitude, longitude, address,
                    rating, reviewsCount, maxGuests, amenities, photos, photoDescriptions);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(resort, "Resort created successfully"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("UPLOAD_ERROR", "Failed to upload photos: " + e.getMessage()));
        }
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

    @PostMapping(value = "/{id}/photos/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Upload Photo (Multipart File)",
            description = "Upload a photo file to a resort using multipart/form-data. " +
                         "The file will be automatically uploaded to GCP Cloud Storage and added to the resort. " +
                         "Supported formats: JPEG, PNG, WebP. Maximum file size: 10MB. Maximum 15 photos per resort. " +
                         "In Swagger UI, click 'Try it out' and use the 'Choose File' button to select an image."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Multipart form data",
            required = true,
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(type = "object", description = "Form data with file and optional description")
            )
    )
    public ResponseEntity<ApiResponse<com.greenfox.backend.modules.resort.dto.PhotoUploadResponse>> uploadPhoto(
            @Parameter(description = "Resort UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(
                    description = "Image file to upload (JPEG, PNG, or WebP). In Swagger UI, use the file picker button.",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Optional photo description", example = "Living room with mountain view")
            @RequestParam(value = "description", required = false) String description) {
        try {
            com.greenfox.backend.modules.resort.dto.PhotoUploadResponse response = 
                    resortService.uploadPhoto(id, file, description);
            return ResponseEntity.ok(ApiResponse.success(response, "Photo uploaded successfully"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("UPLOAD_ERROR", "Failed to upload photo: " + e.getMessage()));
        }
    }

    // @Deprecated - Use POST /{id}/photos/upload instead for multipart file uploads
    // This endpoint is kept for backward compatibility but not recommended for new integrations
    @PostMapping(value = "/{id}/photos", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "[Deprecated] Add Photos (JSON URLs)",
            description = "[DEPRECATED] Add photos to a resort using JSON with URLs (max 15 total). " +
                         "This endpoint is deprecated. Please use POST /{id}/photos/upload for multipart file uploads instead. " +
                         "Accepts application/json."
    )
    @Deprecated
    public ResponseEntity<ApiResponse<ResortResponse>> addPhotos(
            @PathVariable UUID id,
            @Valid @RequestBody List<ResortPhotoRequest> photos) {
        ResortResponse resort = resortService.addPhotos(id, photos);
        return ResponseEntity.ok(ApiResponse.success(resort, "Photos added successfully (Note: Use /photos/upload endpoint for file uploads)"));
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

