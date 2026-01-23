package com.greenfox.backend.modules.resort.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded class for cancellation policy.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationPolicy {
    private CancellationType type;
    private Integer freeCancellationDays;
    private Integer partialRefundPercent;
    private String description;

    public enum CancellationType {
        FREE,
        PARTIAL,
        NON_REFUNDABLE
    }
}
