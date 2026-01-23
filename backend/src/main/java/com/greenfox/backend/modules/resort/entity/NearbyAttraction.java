package com.greenfox.backend.modules.resort.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded class for nearby attractions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyAttraction {
    private String name;
    private String type; // airport, train_station, landmark, beach, restaurant, shopping
    private Double distanceKm;
    private String icon;
}
