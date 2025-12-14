package com.greenfox.backend.modules.promo.repository;

import com.greenfox.backend.modules.promo.entity.Promo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Promo entity operations.
 */
@Repository
public interface PromoRepository extends JpaRepository<Promo, UUID> {

    /**
     * Find active promos for a date range.
     */
    @Query("SELECT p FROM Promo p WHERE p.deleted = false AND p.active = true " +
           "AND p.startDate <= :date AND p.endDate >= :date " +
           "ORDER BY p.discountPercent DESC")
    List<Promo> findActivePromos(@Param("date") LocalDate date);

    /**
     * Find active promo for a specific resort.
     */
    @Query("SELECT p FROM Promo p WHERE p.deleted = false AND p.active = true " +
           "AND p.resort.id = :resortId " +
           "AND p.startDate <= :date AND p.endDate >= :date " +
           "ORDER BY p.discountPercent DESC")
    Optional<Promo> findActivePromoForResort(@Param("resortId") UUID resortId, @Param("date") LocalDate date);

    /**
     * Find all promos for admin.
     */
    Page<Promo> findByDeletedFalse(Pageable pageable);

    Optional<Promo> findByIdAndDeletedFalse(UUID id);

    /**
     * Find promos by resort.
     */
    List<Promo> findByResortIdAndDeletedFalse(UUID resortId);
}

