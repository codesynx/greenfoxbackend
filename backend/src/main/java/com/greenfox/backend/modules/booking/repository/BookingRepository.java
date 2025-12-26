package com.greenfox.backend.modules.booking.repository;

import com.greenfox.backend.modules.booking.entity.Booking;
import com.greenfox.backend.modules.booking.entity.Booking.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Booking entity operations.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /**
     * Find bookings by user.
     */
    Page<Booking> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find booking by ID for a specific user.
     */
    Optional<Booking> findByIdAndUserIdAndDeletedFalse(UUID id, UUID userId);

    /**
     * Find booking by ID (for admin).
     */
    Optional<Booking> findByIdAndDeletedFalse(UUID id);

    /**
     * Find all bookings for admin with optional status filter.
     */
    Page<Booking> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    Page<Booking> findByStatusAndDeletedFalseOrderByCreatedAtDesc(BookingStatus status, Pageable pageable);

    /**
     * Check for overlapping bookings for a resort.
     */
    @Query("SELECT b FROM Booking b WHERE b.resort.id = :resortId " +
           "AND b.deleted = false " +
           "AND b.status NOT IN ('CANCELLED', 'REJECTED') " +
           "AND ((b.checkInDate <= :checkOut AND b.checkOutDate >= :checkIn))")
    List<Booking> findOverlappingBookings(
            @Param("resortId") UUID resortId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    /**
     * Count the number of rooms already booked for a resort in a date range.
     * Executed within a transaction to ensure consistency.
     * The configured READ_COMMITTED isolation level prevents concurrent booking conflicts.
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.resort.id = :resortId " +
           "AND b.deleted = false " +
           "AND b.status NOT IN ('CANCELLED', 'REJECTED') " +
           "AND ((b.checkInDate < :checkOut AND b.checkOutDate > :checkIn))")
    Long countBookedRoomsForDateRange(
            @Param("resortId") UUID resortId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    /**
     * Count bookings by status.
     */
    long countByStatusAndDeletedFalse(BookingStatus status);

    /**
     * Find bookings awaiting admin confirmation.
     */
    List<Booking> findByStatusAndDeletedFalseOrderByCreatedAtAsc(BookingStatus status);
}

