package com.greenfox.backend.modules.ai.config;

import com.greenfox.backend.modules.ai.dto.ResortSearchRequest;
import com.greenfox.backend.modules.ai.service.TripAssistantService;
import com.greenfox.backend.modules.resort.dto.ResortListResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

/**
 * Configuration for Spring AI and function calling (tools).
 * Registers the searchResorts function that the AI can invoke.
 */
@Slf4j
@Configuration
public class AiConfiguration {

    /**
     * Register searchResorts function for AI function calling.
     * This allows the AI to call this function when users ask for resort recommendations.
     *
     * @param tripAssistantService Service containing the actual search logic
     * @return Function bean that Spring AI can invoke
     */
    @Bean
    @Description("Search for resorts and hotels in Kazakhstan based on user criteria. " +
            "Parameters: city (string), amenities (array of strings), maxPrice (number), " +
            "minPrice (number), guests (integer), query (string for general search). " +
            "Returns a list of matching resorts with details like name, city, price, rating, and amenities.")
    public Function<ResortSearchRequest, List<ResortListResponse>> searchResorts(
            TripAssistantService tripAssistantService) {
        log.info("Registering searchResorts function for AI function calling");
        return tripAssistantService.searchResorts();
    }
}
