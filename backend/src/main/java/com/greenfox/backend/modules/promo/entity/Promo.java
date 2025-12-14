package com.greenfox.backend.modules.promo.entity;

import com.greenfox.backend.common.entity.BaseEntity;
import com.greenfox.backend.modules.resort.entity.Resort;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Promo entity representing promotional offers for resorts.
 */
@Entity
@Table(name = "promos", indexes = {
        @Index(name = "idx_promos_dates", columnList = "start_date, end_date"),
        @Index(name = "idx_promos_resort", columnList = "resort_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Promo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resort_id", nullable = false)
    private Resort resort;

    @Column(name = "discount_percent", nullable = false)
    private Integer discountPercent;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "banner_image_url", length = 500)
    private String bannerImageUrl;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Check if promo is currently valid.
     */
    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return active && !today.isBefore(startDate) && !today.isAfter(endDate);
    }
}

