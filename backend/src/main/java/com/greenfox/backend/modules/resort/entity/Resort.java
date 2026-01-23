package com.greenfox.backend.modules.resort.entity;

import com.greenfox.backend.common.entity.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resort entity representing a bookable resort/recreational location.
 */
@Entity
@Table(name = "resorts", indexes = {
        @Index(name = "idx_resorts_city", columnList = "city"),
        @Index(name = "idx_resorts_is_promo", columnList = "is_promo"),
        @Index(name = "idx_resorts_base_price", columnList = "base_price")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SqlResultSetMapping(
        name = "ResortWithDistanceMapping",
        entities = @EntityResult(
                entityClass = Resort.class
        ),
        columns = @ColumnResult(name = "distance", type = Double.class)
)
public class Resort extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    @Builder.Default
    private ResortType type = ResortType.RESORT;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Location coordinates
    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "address", length = 500)
    private String address;

    // Pricing
    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    // Rating (manual input by admin)
    @Column(name = "rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "reviews_count")
    @Builder.Default
    private Integer reviewsCount = 0;

    // Amenities stored as JSONB array
    @Type(JsonType.class)
    @Column(name = "amenities", columnDefinition = "jsonb")
    @Builder.Default
    private List<String> amenities = new ArrayList<>();

    // Photos with descriptions stored as JSONB
    @Type(JsonType.class)
    @Column(name = "photos", columnDefinition = "jsonb")
    @Builder.Default
    private List<ResortPhoto> photos = new ArrayList<>();

    // Capacity
    @Column(name = "max_guests")
    private Integer maxGuests;

    // Total number of rooms/units available for booking
    @Column(name = "total_rooms", nullable = false)
    @Builder.Default
    private Integer totalRooms = 1;

    // Promo flag for sorting priority
    @Column(name = "is_promo", nullable = false)
    @Builder.Default
    private boolean promo = false;

    // Active status
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    // House rules (check-in/out times, policies)
    @Type(JsonType.class)
    @Column(name = "house_rules", columnDefinition = "jsonb")
    private HouseRules houseRules;

    // Property highlights (key features)
    @Type(JsonType.class)
    @Column(name = "highlights", columnDefinition = "jsonb")
    @Builder.Default
    private List<PropertyHighlight> highlights = new ArrayList<>();

    // Neighborhood description
    @Column(name = "neighborhood_description", columnDefinition = "TEXT")
    private String neighborhoodDescription;

    // Transport/getting there description
    @Column(name = "transport_description", columnDefinition = "TEXT")
    private String transportDescription;

    // Nearby attractions with distances
    @Type(JsonType.class)
    @Column(name = "nearby_attractions", columnDefinition = "jsonb")
    @Builder.Default
    private List<NearbyAttraction> nearbyAttractions = new ArrayList<>();

    // Cancellation policy
    @Type(JsonType.class)
    @Column(name = "cancellation_policy", columnDefinition = "jsonb")
    private CancellationPolicy cancellationPolicy;

    // Categorized amenities (for enhanced display)
    @Type(JsonType.class)
    @Column(name = "categorized_amenities", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, List<String>> categorizedAmenities = new HashMap<>();

    @Transient
    private Double distanceKm;

    /**
     * Embedded class for resort photos with descriptions.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResortPhoto {
        private String url;
        private String description;
        private int order;
    }
}

