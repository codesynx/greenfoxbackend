package com.greenfox.backend.modules.resort.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded class for house rules of a resort.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HouseRules {
    private String checkInTime;
    private String checkOutTime;
    private Boolean smokingAllowed;
    private Boolean petsAllowed;
    private Boolean partiesAllowed;
    private String quietHoursStart;
    private String quietHoursEnd;
    private Integer minimumAge;
    private String additionalRules;
}
