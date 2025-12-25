package com.greenfox.backend.modules.booking.controller;

import com.greenfox.backend.common.dto.ApiResponse;
import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.modules.booking.dto.*;
import com.greenfox.backend.modules.booking.service.BookingService;
import com.greenfox.backend.security.UserPrincipal;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * User booking endpoints.
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "User booking endpoints")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/calc")
    @Operation(
            summary = "Calculate Booking Price",
            description = "Calculate total price for a booking without creating it. " +
                         "Checks availability and applies any active promotions."
    )
    public ResponseEntity<ApiResponse<BookingCalcResponse>> calculatePrice(
            @Valid @RequestBody BookingCalcRequest request) {
        BookingCalcResponse response = bookingService.calculatePrice(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(
            summary = "Create Booking",
            description = "Create a new booking request. Returns payment information including Kaspi deep link."
    )
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(principal, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Booking created. Please complete payment."));
    }

    @PostMapping("/{id}/pay")
    @Operation(
            summary = "Confirm Payment (Temporary)",
            description = "Simulate payment confirmation. In production, this would be a Kaspi webhook."
    )
    public ResponseEntity<ApiResponse<BookingResponse>> confirmPayment(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        BookingResponse response = bookingService.confirmPayment(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment confirmed. Awaiting admin confirmation."));
    }

    @PostMapping("/{id}/cancel")
    @Operation(
            summary = "Cancel Booking",
            description = "Request cancellation of a booking"
    )
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CancelBookingRequest request) {
        BookingResponse response = bookingService.cancelBooking(id, principal, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cancellation requested successfully"));
    }

    @GetMapping("/my")
    @Operation(
            summary = "Get My Bookings",
            description = "Get current user's booking history"
    )
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getMyBookings(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PageResponse<BookingResponse> response = bookingService.getUserBookings(principal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get Booking Details",
            description = "Get details of a specific booking"
    )
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        BookingResponse response = bookingService.getUserBooking(id, principal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
