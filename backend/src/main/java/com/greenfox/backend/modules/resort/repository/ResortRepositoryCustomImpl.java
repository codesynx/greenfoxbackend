package com.greenfox.backend.modules.resort.repository;

import com.greenfox.backend.modules.resort.dto.ResortFilterRequest;
import com.greenfox.backend.modules.resort.entity.Resort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ResortRepositoryCustomImpl implements ResortRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public Page<Resort> findAllWithDistance(ResortFilterRequest filter, Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT r.* ");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) ");
        
        StringBuilder commonSql = new StringBuilder("FROM resorts r WHERE r.is_deleted = false AND r.is_active = true ");
        Map<String, Object> params = new HashMap<>();

        // 1. Dynamic Filters
        if (filter.getCity() != null && !filter.getCity().isBlank()) {
            commonSql.append("AND LOWER(r.city) = LOWER(:city) ");
            params.put("city", filter.getCity());
        }

        if (filter.getMinPrice() != null) {
            commonSql.append("AND r.base_price >= :minPrice ");
            params.put("minPrice", filter.getMinPrice());
        }

        if (filter.getMaxPrice() != null) {
            commonSql.append("AND r.base_price <= :maxPrice ");
            params.put("maxPrice", filter.getMaxPrice());
        }

        if (filter.getGuests() != null) {
            commonSql.append("AND r.max_guests >= :guests ");
            params.put("guests", filter.getGuests());
        }

        if (filter.getQuery() != null && !filter.getQuery().isBlank()) {
            commonSql.append("AND (LOWER(r.name) LIKE LOWER(:query) OR LOWER(r.description) LIKE LOWER(:query) OR LOWER(r.city) LIKE LOWER(:query)) ");
            params.put("query", "%" + filter.getQuery() + "%");
        }

        // 2. Distance Calculation (Haversine)
        boolean hasCoords = filter.getUserLatitude() != null && filter.getUserLongitude() != null;
        if (hasCoords) {
            sql.append(", (6371 * acos(LEAST(1.0, GREATEST(-1.0, cos(radians(:lat)) * cos(radians(r.latitude)) * cos(radians(r.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(r.latitude)))))) AS distance ");
            params.put("lat", filter.getUserLatitude());
            params.put("lng", filter.getUserLongitude());
        }

        sql.append(commonSql);
        countSql.append(commonSql);

        // 3. Ordering - prioritize distance when coordinates provided
        if (hasCoords) {
            // Sort by distance first (nearest first), then promo, then rating
            sql.append("ORDER BY distance ASC, r.is_promo DESC, r.rating DESC ");
        } else {
            // Without coordinates, sort by promo first, then rating
            sql.append("ORDER BY r.is_promo DESC, r.rating DESC ");
        }

        // 4. Create Queries
        Query query = hasCoords 
                ? entityManager.createNativeQuery(sql.toString(), "ResortWithDistanceMapping")
                : entityManager.createNativeQuery(sql.toString(), Resort.class);
        
        Query countQuery = entityManager.createNativeQuery(countSql.toString());

        // Set parameters
        params.forEach((key, value) -> {
            query.setParameter(key, value);
            // Only set parameters that exist in the count query (skip lat/lng which are only for distance)
            if (!key.equals("lat") && !key.equals("lng")) {
                countQuery.setParameter(key, value);
            }
        });

        // 5. Pagination
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Resort> content;
        if (hasCoords) {
            List<Object[]> results = query.getResultList();
            content = results.stream().map(row -> {
                Resort r = (Resort) row[0];
                r.setDistanceKm((Double) row[1]);
                return r;
            }).toList();
        } else {
            content = query.getResultList();
        }
        
        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }
}
