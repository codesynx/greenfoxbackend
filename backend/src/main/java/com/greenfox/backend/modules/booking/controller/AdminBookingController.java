package com.greenfox.backend.modules.booking.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.modules.booking.dto.BookingResponse;
import com.greenfox.backend.modules.booking.dto.UpdateBookingStatusRequest;
import com.greenfox.backend.modules.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin booking management endpoints.
 */
@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Bookings", description = "Booking management endpoints (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    @Operation(
            summary = "Get All Bookings",
            description = "Get all bookings with optional status filter"
    )
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getAllBookings(
            @Parameter(description = "Filter by status (PENDING, PAID_WAITING, CONFIRMED, COMPLETED, CANCELLED)")
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BookingResponse> response = bookingService.getAllBookings(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Update Booking Status",
            description = "Update booking status. Triggers notification to user."
    )
    public ResponseEntity<ApiResponse<BookingResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBookingStatusRequest request) {
        BookingResponse response = bookingService.updateBookingStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Booking status updated"));
    }
}

