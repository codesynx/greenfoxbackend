package com.greenfox.backend.modules.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating a booking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create booking request")
public class CreateBookingRequest {

    @NotNull(message = "Resort ID is required")
    @Schema(description = "Resort ID")
    private UUID resortId;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    @Schema(description = "Check-in date")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    @Schema(description = "Check-out date")
    private LocalDate checkOutDate;

    @NotNull(message = "Number of adults is required")
    @Min(value = 1, message = "At least 1 adult is required")
    @Schema(description = "Number of adults", example = "2")
    private Integer adults;

    @Min(value = 0, message = "Children count cannot be negative")
    @Schema(description = "Number of children", example = "1")
    private Integer children;

    // Guest information
    @NotBlank(message = "Guest full name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    @Schema(description = "Guest full name", example = "Нұрлан Әлібек")
    private String guestFullName;

    @NotBlank(message = "ID number is required")
    @Pattern(regexp = "^[0-9A-Za-z]{6,20}$", message = "Invalid ID number format")
    @Schema(description = "IIN or Passport number", example = "990101350123")
    private String idNumber;

    @NotBlank(message = "ID type is required")
    @Pattern(regexp = "^(IIN|PASSPORT)$", message = "ID type must be IIN or PASSPORT")
    @Schema(description = "Type of ID document", example = "IIN")
    private String idType;

    @Pattern(regexp = "^\\+?[1-9]\\d{10,14}$", message = "Invalid phone number format")
    @Schema(description = "Contact phone number", example = "+77001234567")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    @Schema(description = "Contact email")
    private String email;

    @Size(max = 1000, message = "Special requests must not exceed 1000 characters")
    @Schema(description = "Special requests or notes")
    private String specialRequests;

    @Schema(description = "Payment method details")
    private PaymentMethod paymentMethod;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentMethod {
        private String type;
        private String cardToken;
        private String last4;
    }
}
