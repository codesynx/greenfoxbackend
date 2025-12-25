package com.greenfox.backend.modules.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cancel booking request")
public class CancelBookingRequest {
    @Size(max = 100, message = "Reason must not exceed 100 characters")
    @Schema(description = "Cancellation reason", example = "Change of plans")
    private String reason;
}
