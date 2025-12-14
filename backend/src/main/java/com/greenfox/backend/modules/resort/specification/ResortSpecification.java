package com.greenfox.backend.modules.resort.specification;

import com.greenfox.backend.modules.resort.dto.ResortFilterRequest;
import com.greenfox.backend.modules.resort.entity.Resort;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA Specifications for Resort filtering.
 */
public class ResortSpecification {

    private ResortSpecification() {
    }

    public static Specification<Resort> withFilters(ResortFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base conditions: not deleted and active
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(cb.equal(root.get("active"), true));

            // City filter
            if (filter.getCity() != null && !filter.getCity().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("city")), filter.getCity().toLowerCase()));
            }

            // Price range filter
            if (filter.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("basePrice"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), filter.getMaxPrice()));
            }

            // Guest capacity filter
            if (filter.getGuests() != null && filter.getGuests() > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxGuests"), filter.getGuests()));
            }

            // Search query (name or city)
            if (filter.getQuery() != null && !filter.getQuery().isBlank()) {
                String queryPattern = "%" + filter.getQuery().toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), queryPattern);
                Predicate cityPredicate = cb.like(cb.lower(root.get("city")), queryPattern);
                predicates.add(cb.or(namePredicate, cityPredicate));
            }

            // Order by promo first, then by rating
            query.orderBy(
                    cb.desc(root.get("promo")),
                    cb.desc(root.get("rating"))
            );

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

