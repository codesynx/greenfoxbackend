package com.greenfox.backend.modules.resort.entity;

import com.greenfox.backend.common.entity.BaseEntity;
import com.greenfox.backend.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Favorite resort entity for user favorites.
 */
@Entity
@Table(name = "favorites", indexes = {
        @Index(name = "idx_favorites_user", columnList = "user_id"),
        @Index(name = "idx_favorites_resort", columnList = "resort_id"),
        @Index(name = "idx_favorites_user_resort", columnList = "user_id,resort_id", unique = true)
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favorite extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resort_id", nullable = false)
    private Resort resort;
}

