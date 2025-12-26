package com.greenfox.backend.modules.ai.tools;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.greenfox.backend.modules.ai.service.ResortSearchContext;
import com.greenfox.backend.modules.resort.entity.Resort;
import com.greenfox.backend.modules.resort.repository.ResortRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ResortTools {

    private final ResortRepository resortRepository;

    @Bean
    @Description("Search for resorts based on city, price, and amenities")
    public Function<SearchResortsRequest, List<Resort>> searchResorts() {
        return request -> {
            log.info("Searching resorts with request: {}", request);
            
            Specification<Resort> spec = Specification.where((root, query, cb) -> cb.equal(root.get("active"), true))
                    .and((root, query, cb) -> cb.equal(root.get("deleted"), false));

            if (request.city() != null && !request.city().isEmpty()) {
                spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("city")), request.city().toLowerCase()));
            }

            if (request.maxPrice() != null) {
                spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("basePrice"), request.maxPrice()));
            }

            // Amenities filter could be complex with JSONB, for now basic implementation or skip if too complex without native query
            // Assuming simple containment check if possible, otherwise we might need native queries.
            // But let's assume the LLM will filter or we just return best matches.
            // Ideally we should filter by amenities. Since it is jsonb, we need a special specification.
            // For now, let's filter in memory if the dataset is small, or just ignore for MVP if difficult.
            // Or use the custom repository method if available.

            List<Resort> resorts = resortRepository.findAll(spec);
            
            // In-memory filter for amenities (simplified)
            if (request.amenities() != null && !request.amenities().isEmpty()) {
                resorts = resorts.stream()
                        .filter(r -> r.getAmenities() != null && r.getAmenities().containsAll(request.amenities()))
                        .toList();
            }

            // Limit results
            if (resorts.size() > 5) {
                resorts = resorts.subList(0, 5);
            }

            ResortSearchContext.addResorts(resorts);
            return resorts;
        };
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("Request to search for resorts")
    public record SearchResortsRequest(
            @JsonPropertyDescription("The city to search for resorts in") String city,
            @JsonPropertyDescription("The maximum price per night") BigDecimal maxPrice,
            @JsonPropertyDescription("List of required amenities (e.g., 'WiFi', 'Pool', 'Spa')") List<String> amenities) {
    }
}
