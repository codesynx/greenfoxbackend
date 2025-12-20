package com.greenfox.backend.modules.resort.repository;

import com.greenfox.backend.modules.resort.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Favorite entity operations.
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    /**
     * Check if a resort is favorited by a user.
     */
    boolean existsByUserIdAndResortIdAndDeletedFalse(UUID userId, UUID resortId);

    /**
     * Find favorite by user and resort (excluding deleted).
     */
    Optional<Favorite> findByUserIdAndResortIdAndDeletedFalse(UUID userId, UUID resortId);

    /**
     * Find favorite by user and resort (including deleted).
     */
    Optional<Favorite> findByUserIdAndResortId(UUID userId, UUID resortId);

    /**
     * Get all favorites for a user.
     */
    Page<Favorite> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Count favorites for a user.
     */
    long countByUserIdAndDeletedFalse(UUID userId);
}

