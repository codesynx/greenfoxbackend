package com.greenfox.backend.modules.booking.entity;

import com.greenfox.backend.common.entity.BaseEntity;
import com.greenfox.backend.modules.resort.entity.Resort;
import com.greenfox.backend.modules.user.entity.User;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Booking entity representing a resort reservation.
 * Status flow: PENDING -> PAID_WAITING -> CONFIRMED -> COMPLETED (or CANCELLED)
 */
@Entity
@Table(name = "bookings", indexes = {
        @Index(name = "idx_bookings_user", columnList = "user_id"),
        @Index(name = "idx_bookings_resort", columnList = "resort_id"),
        @Index(name = "idx_bookings_status", columnList = "status"),
        @Index(name = "idx_bookings_dates", columnList = "check_in_date, check_out_date")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resort_id", nullable = false)
    private Resort resort;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "adults", nullable = false)
    private Integer adults;

    @Column(name = "children")
    @Builder.Default
    private Integer children = 0;

    // Guest information stored as JSONB
    @Type(JsonType.class)
    @Column(name = "guest_info", columnDefinition = "jsonb")
    private GuestInfo guestInfo;

    // Pricing
    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "discount_percent")
    @Builder.Default
    private Integer discountPercent = 0;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "nights", nullable = false)
    private Integer nights;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    // Payment info
    @Column(name = "kaspi_invoice_id", length = 100)
    private String kaspiInvoiceId;

    @Column(name = "kaspi_deep_link", length = 1000)
    private String kaspiDeepLink;

    @Column(name = "payment_confirmed_at")
    private java.time.LocalDateTime paymentConfirmedAt;

    // Admin notes
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    // Cancellation info
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    /**
     * Booking statuses with their flow.
     */
    public enum BookingStatus {
        PENDING,        // Just created, awaiting payment
        PAID_WAITING,   // Payment received, awaiting admin confirmation
        CONFIRMED,      // Admin confirmed the booking
        COMPLETED,      // Stay completed
        CANCELLED,      // Booking cancelled
        CANCELLATION_REQUESTED // User requested cancellation
    }

    /**
     * Guest information for booking.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GuestInfo {
        private String fullName;
        private String idNumber;        // IIN or passport number
        private String idType;          // "IIN" or "PASSPORT"
        private String phoneNumber;
        private String email;
        private String specialRequests;
    }
}
