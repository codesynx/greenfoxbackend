package com.greenfox.backend.modules.ai.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.greenfox.backend.common.dto.PageResponse;
import com.greenfox.backend.modules.ai.dto.ResortRecommendation;
import com.greenfox.backend.modules.resort.dto.ResortFilterRequest;
import com.greenfox.backend.modules.resort.dto.ResortListResponse;
import com.greenfox.backend.modules.resort.entity.Resort;
import com.greenfox.backend.modules.resort.repository.ResortRepository;
import com.greenfox.backend.modules.resort.service.ResortService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Tool for searching resorts, designed for Google Gen AI SDK automatic function calling.
 * Methods must be public static for the SDK to invoke them automatically.
 */
@Slf4j
@Component
public class ResortSearchTool {

    private static ResortService resortService;
    private static ObjectMapper objectMapper;

    // Thread-safe storage for last search results
    @Getter
    private static final ThreadLocal<List<ResortRecommendation>> lastResults = ThreadLocal.withInitial(ArrayList::new);

    @Autowired
    public void setResortService(ResortService resortService) {
        ResortSearchTool.resortService = resortService;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        ResortSearchTool.objectMapper = objectMapper;
    }

    /**
     * Search for resorts based on user criteria.
     * This method is called by Google Gen AI SDK via automatic function calling.
     * Must be public static for the SDK to invoke it.
     *
     * @param city City or destination name (e.g., 'Alakol', 'Almaty', 'Shymbulak')
     * @param minPrice Minimum price per night in KZT (optional, use null if not specified)
     * @param maxPrice Maximum price per night in KZT (optional, use null if not specified)
     * @param guests Number of guests (optional, use null if not specified)
     * @param query Search query for resort name or description (optional)
     * @return JSON string with list of matching resorts
     */
    public static String searchResorts(
            String city,
            String minPrice,
            String maxPrice,
            String guests,
            String query
    ) {
        log.info("AI Tool: searchResorts called with city={}, minPrice={}, maxPrice={}, guests={}, query={}",
                city, minPrice, maxPrice, guests, query);

        // Parse optional parameters
        BigDecimal minPriceDecimal = parsePrice(minPrice);
        BigDecimal maxPriceDecimal = parsePrice(maxPrice);
        Integer guestsInt = parseInteger(guests);

        // Build filter request
        ResortFilterRequest filter = ResortFilterRequest.builder()
                .city(city)
                .minPrice(minPriceDecimal)
                .maxPrice(maxPriceDecimal)
                .guests(guestsInt)
                .query(query)
                .build();

        // Get top 5 results sorted by rating
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "rating"));
        
        // Use optimized method to avoid N+1 queries
        List<Resort> resorts = resortService.searchResortsForAi(filter, pageable);

        // Map to recommendations
        List<ResortRecommendation> recommendations = resorts.stream()
                .map(ResortSearchTool::mapEntityToRecommendation)
                .toList();

        // Store results for later retrieval
        lastResults.get().clear();
        lastResults.get().addAll(recommendations);

        log.info("AI Tool: searchResorts returned {} results", recommendations.size());

        // Return JSON string for the AI to process
        try {
            return objectMapper.writeValueAsString(recommendations);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize recommendations", e);
            return "[]";
        }
    }

    /**
     * Clear the stored search results.
     */
    public static void clearResults() {
        lastResults.remove();
    }

    /**
     * Get the last search results.
     */
    public static List<ResortRecommendation> getLastSearchResults() {
        return List.copyOf(lastResults.get());
    }

    private static BigDecimal parsePrice(String price) {
        if (price == null || price.isBlank() || "null".equalsIgnoreCase(price)) {
            return null;
        }
        try {
            return new BigDecimal(price.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            log.warn("Failed to parse price: {}", price);
            return null;
        }
    }

    private static Integer parseInteger(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer: {}", value);
            return null;
        }
    }

    /**
     * Map a Resort entity directly to ResortRecommendation.
     */
    private static ResortRecommendation mapEntityToRecommendation(Resort resort) {
        String mainPhotoUrl = null;
        if (resort.getPhotos() != null && !resort.getPhotos().isEmpty()) {
            mainPhotoUrl = resort.getPhotos().get(0).getUrl();
        }

        return ResortRecommendation.builder()
                .id(resort.getId())
                .name(resort.getName())
                .city(resort.getCity())
                .basePrice(resort.getBasePrice())
                .rating(resort.getRating())
                .mainPhotoUrl(mainPhotoUrl)
                .maxGuests(resort.getMaxGuests())
                .description(resort.getDescription())
                .amenities(resort.getAmenities())
                .build();
    }

    /**
     * Get a list of all available cities/destinations.
     * This method is called by Google Gen AI SDK via automatic function calling.
     *
     * @return JSON string with list of cities
     */
    public static String getAllCities() {
        log.info("AI Tool: getAllCities called");
        List<String> cities = resortService.getAllCities();
        try {
            return objectMapper.writeValueAsString(cities);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize cities", e);
            return "[]";
        }
    }

    /**
     * Get detailed information about a specific resort.
     * This method is called by Google Gen AI SDK via automatic function calling.
     *
     * @param id The ID of the resort
     * @return JSON string with resort details
     */
    public static String getResortDetails(String id) {
        log.info("AI Tool: getResortDetails called with id={}", id);
        try {
            java.util.UUID uuid = java.util.UUID.fromString(id);
            Resort resort = resortService.getResortEntityById(uuid);
            ResortRecommendation recommendation = mapEntityToRecommendation(resort);
            return objectMapper.writeValueAsString(recommendation);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", id);
            return "{\"error\": \"Invalid ID format\"}";
        } catch (Exception e) {
            log.error("Error getting resort details", e);
            return "{\"error\": \"Resort not found\"}";
        }
    }
}
