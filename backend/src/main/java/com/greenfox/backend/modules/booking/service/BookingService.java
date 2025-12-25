package com.greenfox.backend.modules.booking.service;

import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.common.exception.BadRequestException;
import com.greenfox.backend.common.exception.ResourceNotFoundException;
import com.greenfox.backend.modules.booking.dto.*;
import com.greenfox.backend.modules.booking.entity.Booking;
import com.greenfox.backend.modules.booking.entity.Booking.BookingStatus;
import com.greenfox.backend.modules.booking.repository.BookingRepository;
import com.greenfox.backend.modules.notification.service.NotificationService;
import com.greenfox.backend.modules.promo.entity.Promo;
import com.greenfox.backend.modules.promo.repository.PromoRepository;
import com.greenfox.backend.modules.resort.entity.Resort;
import com.greenfox.backend.modules.resort.repository.ResortRepository;
import com.greenfox.backend.modules.user.entity.User;
import com.greenfox.backend.modules.user.repository.UserRepository;
import com.greenfox.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for booking management - the core business logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ResortRepository resortRepository;
    private final PromoRepository promoRepository;
    private final UserRepository userRepository;
    private final KaspiService kaspiService;
    private final NotificationService notificationService;

    /**
     * Calculate booking price without creating a booking.
     */
    @Transactional(readOnly = true)
    public BookingCalcResponse calculatePrice(BookingCalcRequest request) {
        validateDates(request.getCheckInDate(), request.getCheckOutDate());

        Resort resort = resortRepository.findByIdAndDeletedFalseAndActiveTrue(request.getResortId())
                .orElseThrow(() -> new ResourceNotFoundException("Resort", "id", request.getResortId()));

        int totalGuests = request.getAdults() + (request.getChildren() != null ? request.getChildren() : 0);
        if (resort.getMaxGuests() != null && totalGuests > resort.getMaxGuests()) {
            return BookingCalcResponse.builder()
                    .resortId(resort.getId())
                    .resortName(resort.getName())
                    .available(false)
                    .unavailableReason("Maximum capacity is " + resort.getMaxGuests() + " guests")
                    .build();
        }

        // Check availability
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                request.getResortId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        if (!overlapping.isEmpty()) {
            return BookingCalcResponse.builder()
                    .resortId(resort.getId())
                    .resortName(resort.getName())
                    .available(false)
                    .unavailableReason("Resort is not available for selected dates")
                    .build();
        }

        // Calculate pricing
        int nights = (int) ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal baseTotal = resort.getBasePrice().multiply(BigDecimal.valueOf(nights));

        // Check for active promo
        Optional<Promo> activePromo = promoRepository.findActivePromoForResort(
                request.getResortId(), LocalDate.now());

        int discountPercent = 0;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal discountedBase = baseTotal;

        if (activePromo.isPresent()) {
            discountPercent = activePromo.get().getDiscountPercent();
            discountAmount = baseTotal
                    .multiply(BigDecimal.valueOf(discountPercent))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            discountedBase = baseTotal.subtract(discountAmount);
        }

        // Calculate tax (10% of discounted base)
        BigDecimal taxAmount = discountedBase.multiply(BigDecimal.valueOf(0.10))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPrice = discountedBase.add(taxAmount);

        return BookingCalcResponse.builder()
                .resortId(resort.getId())
                .resortName(resort.getName())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .nights(nights)
                .adults(request.getAdults())
                .children(request.getChildren() != null ? request.getChildren() : 0)
                .basePricePerNight(resort.getBasePrice())
                .baseTotal(baseTotal)
                .discountPercent(discountPercent)
                .discountAmount(discountAmount)
                .discountedPrice(discountedBase)
                .tax(taxAmount)
                .totalPrice(totalPrice)
                .hasPromo(activePromo.isPresent())
                .available(true)
                .build();
    }

    /**
     * Create a new booking.
     */
    @Transactional
    public BookingResponse createBooking(UserPrincipal principal, CreateBookingRequest request) {
        validateDates(request.getCheckInDate(), request.getCheckOutDate());

        User user = userRepository.findByIdAndDeletedFalse(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        Resort resort = resortRepository.findByIdAndDeletedFalseAndActiveTrue(request.getResortId())
                .orElseThrow(() -> new ResourceNotFoundException("Resort", "id", request.getResortId()));

        // Check availability again (race condition protection)
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                request.getResortId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Resort is not available for selected dates");
        }

        // Calculate pricing
        int nights = (int) ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal baseTotal = resort.getBasePrice().multiply(BigDecimal.valueOf(nights));

        // Check for active promo
        Optional<Promo> activePromo = promoRepository.findActivePromoForResort(
                request.getResortId(), LocalDate.now());

        int discountPercent = 0;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal discountedBase = baseTotal;

        if (activePromo.isPresent()) {
            discountPercent = activePromo.get().getDiscountPercent();
            discountAmount = baseTotal
                    .multiply(BigDecimal.valueOf(discountPercent))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            discountedBase = baseTotal.subtract(discountAmount);
        }

        // Calculate tax (10% of discounted base)
        BigDecimal taxAmount = discountedBase.multiply(BigDecimal.valueOf(0.10))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPrice = discountedBase.add(taxAmount);

        // Create guest info
        Booking.GuestInfo guestInfo = Booking.GuestInfo.builder()
                .fullName(request.getGuestFullName())
                .idNumber(request.getIdNumber())
                .idType(request.getIdType())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .specialRequests(request.getSpecialRequests())
                .build();

        // Create booking
        Booking booking = Booking.builder()
                .user(user)
                .resort(resort)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .adults(request.getAdults())
                .children(request.getChildren() != null ? request.getChildren() : 0)
                .guestInfo(guestInfo)
                .basePrice(baseTotal)
                .discountPercent(discountPercent)
                .discountAmount(discountAmount)
                .totalPrice(totalPrice)
                .nights(nights)
                .status(BookingStatus.PENDING)
                .paymentConfirmedAt(request.getPaymentMethod() != null ? LocalDateTime.now() : null)
                .build();

        // Generate Kaspi payment info
        booking.setKaspiInvoiceId(kaspiService.generateInvoiceId(booking.getId() != null 
                ? booking.getId() : UUID.randomUUID()));
        
        Booking saved = bookingRepository.save(booking);
        
        // Generate deep link with actual ID
        saved.setKaspiDeepLink(kaspiService.generateDeepLink(saved.getId(), totalPrice));
        saved.setKaspiInvoiceId(kaspiService.generateInvoiceId(saved.getId()));
        saved = bookingRepository.save(saved);

        log.info("Booking created: {} for resort: {} by user: {}", 
                saved.getId(), resort.getName(), user.getPhoneNumber());

        // Create notification for user
        notificationService.createBookingNotification(saved, "notification.booking.created");

        return toResponse(saved);
    }

    /**
     * Simulate payment confirmation (temporary endpoint).
     * In production, this would be a webhook from Kaspi.
     */
    @Transactional
    public BookingResponse confirmPayment(UUID bookingId, UserPrincipal principal) {
        Booking booking = bookingRepository.findByIdAndUserIdAndDeletedFalse(bookingId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Booking is not in PENDING status");
        }

        // In real implementation, verify with Kaspi
        // boolean verified = kaspiService.verifyPayment(booking.getKaspiInvoiceId());

        booking.setStatus(BookingStatus.PAID_WAITING);
        booking.setPaymentConfirmedAt(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);

        log.info("Payment confirmed for booking: {}", bookingId);

        // Notify admin about new paid booking
        notificationService.createAdminNotification(
                "New booking awaiting confirmation: " + booking.getResort().getName(),
                saved.getId()
        );

        // Notify user
        notificationService.createBookingNotification(saved, 
                "Payment received. Awaiting admin confirmation.");

        return toResponse(saved);
    }

    /**
     * Get user's booking history.
     */
    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getUserBookings(UserPrincipal principal, Pageable pageable) {
        Page<Booking> bookings = bookingRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(
                principal.getId(), pageable);

        List<BookingResponse> content = bookings.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.from(bookings, content);
    }

    /**
     * Get booking by ID for user.
     */
    @Transactional(readOnly = true)
    public BookingResponse getUserBooking(UUID bookingId, UserPrincipal principal) {
        Booking booking = bookingRepository.findByIdAndUserIdAndDeletedFalse(bookingId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        return toResponse(booking);
    }

    // ============== Admin Operations ==============

    /**
     * Get all bookings for admin.
     */
    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getAllBookings(String status, Pageable pageable) {
        Page<Booking> bookings;
        
        if (status != null && !status.isBlank()) {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            bookings = bookingRepository.findByStatusAndDeletedFalseOrderByCreatedAtDesc(bookingStatus, pageable);
        } else {
            bookings = bookingRepository.findByDeletedFalseOrderByCreatedAtDesc(pageable);
        }

        List<BookingResponse> content = bookings.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.from(bookings, content);
    }

    /**
     * Request booking cancellation.
     */
    @Transactional
    public void cancelBooking(UUID bookingId, String reason, UserPrincipal principal) {
        Booking booking = bookingRepository.findByIdAndUserIdAndDeletedFalse(bookingId, principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BadRequestException("Only PENDING or CONFIRMED bookings can be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLATION_PENDING);
        booking.setCancellationReason(reason);
        Booking saved = bookingRepository.save(booking);

        log.info("Booking cancellation requested: {}", bookingId);

        // Notify user
        notificationService.createBookingNotification(saved, "notification.booking.cancellation_under_review");
        
        // Notify admin
        notificationService.createAdminNotification(
                "Cancellation requested for booking: " + booking.getResort().getName(),
                saved.getId()
        );
    }

    /**
     * Update booking status (Admin only).
     */
    @Transactional
    public BookingResponse updateBookingStatus(UUID bookingId, UpdateBookingStatusRequest request) {
        Booking booking = bookingRepository.findByIdAndDeletedFalse(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        BookingStatus newStatus = BookingStatus.valueOf(request.getStatus().toUpperCase());
        BookingStatus currentStatus = booking.getStatus();

        // Validate status transition
        validateStatusTransition(currentStatus, newStatus);

        booking.setStatus(newStatus);
        if (request.getAdminNotes() != null) {
            booking.setAdminNotes(request.getAdminNotes());
        }

        Booking saved = bookingRepository.save(booking);
        log.info("Booking {} status updated: {} -> {}", bookingId, currentStatus, newStatus);

        // Send notification to user
        String message = switch (newStatus) {
            case CONFIRMED -> "notification.booking.confirmed";
            case REJECTED -> "notification.booking.rejected";
            case COMPLETED -> "notification.booking.completed";
            case CANCELLED -> "notification.booking.refund_processed";
            default -> "Booking status updated to: " + newStatus;
        };
        notificationService.createBookingNotification(saved, message);

        return toResponse(saved);
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isAfter(checkOut)) {
            throw new BadRequestException("Check-in date must be before check-out date");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BadRequestException("Check-in date cannot be in the past");
        }
    }

    private void validateStatusTransition(BookingStatus current, BookingStatus newStatus) {
        boolean valid = switch (current) {
            case PENDING -> newStatus == BookingStatus.CONFIRMED || newStatus == BookingStatus.REJECTED || newStatus == BookingStatus.CANCELLED || newStatus == BookingStatus.CANCELLATION_PENDING;
            case PAID_WAITING -> newStatus == BookingStatus.CONFIRMED || newStatus == BookingStatus.REJECTED || newStatus == BookingStatus.CANCELLED;
            case CONFIRMED -> newStatus == BookingStatus.COMPLETED || newStatus == BookingStatus.CANCELLATION_PENDING || newStatus == BookingStatus.CANCELLED;
            case CANCELLATION_PENDING -> newStatus == BookingStatus.CANCELLED || newStatus == BookingStatus.CONFIRMED || newStatus == BookingStatus.REJECTED;
            case REJECTED, COMPLETED, CANCELLED -> false;
        };

        if (!valid) {
            throw new BadRequestException(
                    String.format("Cannot transition from %s to %s", current, newStatus));
        }
    }

    private BookingResponse toResponse(Booking booking) {
        Resort resort = booking.getResort();
        String mainPhoto = resort.getPhotos() != null && !resort.getPhotos().isEmpty()
                ? resort.getPhotos().get(0).getUrl()
                : null;

        return BookingResponse.builder()
                .id(booking.getId())
                .resortId(resort.getId())
                .resortName(resort.getName())
                .resortCity(resort.getCity())
                .resortPhotoUrl(mainPhoto)
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .nights(booking.getNights())
                .adults(booking.getAdults())
                .children(booking.getChildren())
                .guestInfo(booking.getGuestInfo())
                .basePrice(booking.getBasePrice())
                .discountPercent(booking.getDiscountPercent())
                .discountAmount(booking.getDiscountAmount())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus().name())
                .kaspiDeepLink(booking.getKaspiDeepLink())
                .paymentRequired(booking.getStatus() == BookingStatus.PENDING)
                .createdAt(booking.getCreatedAt())
                .paymentConfirmedAt(booking.getPaymentConfirmedAt())
                .build();
    }
}
