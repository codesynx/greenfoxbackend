package com.greenfox.backend.modules.resort.repository;

import com.greenfox.backend.modules.resort.entity.Resort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Resort entity with custom query methods.
 */
@Repository
public interface ResortRepository extends JpaRepository<Resort, UUID>, JpaSpecificationExecutor<Resort>, ResortRepositoryCustom {

    Optional<Resort> findByIdAndDeletedFalseAndActiveTrue(UUID id);

    Optional<Resort> findByIdAndDeletedFalse(UUID id);

    Page<Resort> findByDeletedFalseAndActiveTrue(Pageable pageable);

    /**
     * Find all active resorts (for RAG ingestion).
     */
    List<Resort> findByDeletedFalseAndActiveTrue();

    /**
     * Find resorts by city with pagination.
     */
    Page<Resort> findByCityIgnoreCaseAndDeletedFalseAndActiveTrue(String city, Pageable pageable);

    /**
     * Find resorts with price range.
     */
    Page<Resort> findByBasePriceBetweenAndDeletedFalseAndActiveTrue(
            BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    /**
     * Get all unique cities for filtering.
     */
    @Query("SELECT DISTINCT r.city FROM Resort r WHERE r.deleted = false AND r.active = true ORDER BY r.city")
    List<String> findAllCities();

    /**
     * Search resorts by name or city.
     */
    @Query("SELECT r FROM Resort r WHERE r.deleted = false AND r.active = true " +
           "AND (LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(r.city) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Resort> searchByNameOrCity(@Param("query") String query, Pageable pageable);

    /**
     * Find resorts sorted by promo status and optionally by distance.
     * Promo resorts appear first.
     */
    @Query("SELECT r FROM Resort r WHERE r.deleted = false AND r.active = true " +
           "ORDER BY r.promo DESC, r.rating DESC")
    Page<Resort> findAllSortedByPromoAndRating(Pageable pageable);

    /**
     * Count active resorts.
     */
    long countByDeletedFalseAndActiveTrue();

    /**
     * Find promo resorts.
     */
    Page<Resort> findByPromoTrueAndDeletedFalseAndActiveTrue(Pageable pageable);
}

