package com.greenfox.backend.modules.resort.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded class for property highlights.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyHighlight {
    private String icon;
    private String titleKey;
    private String description;
}
