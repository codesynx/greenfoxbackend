package com.greenfox.backend.modules.resort.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.modules.resort.dto.ResortFilterRequest;
import com.greenfox.backend.modules.resort.dto.ResortListResponse;
import com.greenfox.backend.modules.resort.dto.ResortResponse;
import com.greenfox.backend.modules.resort.service.ResortService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Public resort endpoints for browsing resorts.
 */
@RestController
@RequestMapping("/api/v1/resorts")
@RequiredArgsConstructor
@Tag(name = "Resorts", description = "Public resort browsing endpoints")
public class ResortController {

    private final ResortService resortService;

    @GetMapping
    @Operation(
            summary = "List Resorts",
            description = "Get paginated list of resorts with optional filters. " +
                         "Sorted by promotional status first, then by rating."
    )
    public ResponseEntity<ApiResponse<PageResponse<ResortListResponse>>> getResorts(
            @Parameter(description = "Filter by city")
            @RequestParam(required = false) String city,
            
            @Parameter(description = "Minimum price")
            @RequestParam(required = false) BigDecimal minPrice,
            
            @Parameter(description = "Maximum price")
            @RequestParam(required = false) BigDecimal maxPrice,
            
            @Parameter(description = "Minimum number of guests")
            @RequestParam(required = false) Integer guests,
            
            @Parameter(description = "Search query (name/city)")
            @RequestParam(required = false) String query,
            
            @Parameter(description = "User's latitude for sorting")
            @RequestParam(required = false) BigDecimal lat,
            
            @Parameter(description = "User's longitude for sorting")
            @RequestParam(required = false) BigDecimal lng,
            
            @PageableDefault(size = 20, sort = "rating", direction = Sort.Direction.DESC) 
            Pageable pageable) {

        ResortFilterRequest filter = ResortFilterRequest.builder()
                .city(city)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .guests(guests)
                .query(query)
                .userLatitude(lat)
                .userLongitude(lng)
                .build();

        PageResponse<ResortListResponse> resorts = resortService.getResorts(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(resorts));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get Resort Details",
            description = "Get detailed information about a specific resort"
    )
    public ResponseEntity<ApiResponse<ResortResponse>> getResort(
            @PathVariable UUID id) {
        ResortResponse resort = resortService.getResortById(id);
        return ResponseEntity.ok(ApiResponse.success(resort));
    }

    @GetMapping("/cities")
    @Operation(
            summary = "Get Cities",
            description = "Get list of all cities with resorts for filtering"
    )
    public ResponseEntity<ApiResponse<List<String>>> getCities() {
        List<String> cities = resortService.getAllCities();
        return ResponseEntity.ok(ApiResponse.success(cities));
    }
}

