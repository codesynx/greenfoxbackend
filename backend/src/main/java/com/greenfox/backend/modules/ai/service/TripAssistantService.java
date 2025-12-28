package com.greenfox.backend.modules.ai.service;

import com.greenfox.backend.modules.ai.dto.AiChatResponse;
import com.greenfox.backend.modules.ai.dto.ResortSearchRequest;
import com.greenfox.backend.modules.resort.dto.ResortFilterRequest;
import com.greenfox.backend.modules.resort.dto.ResortListResponse;
import com.greenfox.backend.modules.resort.entity.Resort;
import com.greenfox.backend.modules.resort.mapper.ResortMapper;
import com.greenfox.backend.modules.resort.repository.ResortRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Service for AI-powered trip planning and resort recommendations.
 * Uses Spring AI with Google Gemini and function calling (tools) to search resorts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TripAssistantService {

    private final ChatClient.Builder chatClientBuilder;
    private final ResortRepository resortRepository;
    private final ResortMapper resortMapper;

    private static final int MAX_RESULTS = 10;

    // Multi-language system prompts
    private static final Map<String, String> SYSTEM_PROMPTS = Map.of(
            "en", """
                You are a premium travel concierge for GreenFox, a resort and hotel booking application serving Kazakhstan.
                Your role is to help users find the perfect accommodation for their trips.

                IMPORTANT INSTRUCTIONS:
                - Always answer in English.
                - When users ask for hotel, resort, or accommodation suggestions, you MUST use the searchResorts tool.
                - DO NOT make up or invent resort information. Only use data from the searchResorts function.
                - Be friendly, professional, and helpful.
                - If the user's request is unclear, ask clarifying questions.
                - Suggest popular destinations like Almaty, Shymkent, Shymbulak, and Alakol when relevant.
                - Always mention the resort name, city, price, and key amenities in your recommendations.
                """,
            "ru", """
                Вы - премиум консьерж по путешествиям для GreenFox, приложения для бронирования курортов и отелей в Казахстане.
                Ваша задача - помочь пользователям найти идеальное жилье для их поездок.

                ВАЖНЫЕ ИНСТРУКЦИИ:
                - Всегда отвечайте на русском языке.
                - Когда пользователи спрашивают о предложениях отелей, курортов или жилья, вы ДОЛЖНЫ использовать инструмент searchResorts.
                - НЕ придумывайте информацию о курортах. Используйте только данные из функции searchResorts.
                - Будьте дружелюбны, профессиональны и полезны.
                - Если запрос пользователя неясен, задавайте уточняющие вопросы.
                - Предлагайте популярные направления, такие как Алматы, Шымкент, Шымбулак и Алаколь, когда это уместно.
                - Всегда упоминайте название курорта, город, цену и основные удобства в своих рекомендациях.
                """,
            "kk", """
                Сіз GreenFox, Қазақстандағы курорттар мен қонақүйлерді брондау қосымшасының премиум саяхат консьержісісіз.
                Сіздің рөліңіз - пайдаланушыларға олардың саяхаттары үшін тамаша баламаны табуға көмектесу.

                МАҢЫЗДЫ НҰСҚАУЛАР:
                - Әрқашан қазақ тілінде жауап беріңіз.
                - Пайдаланушылар қонақүй, курорт немесе баспана ұсыныстарын сұраған кезде, сіз searchResorts құралын МІНДЕТТІ түрде пайдалануыңыз керек.
                - Курорт туралы ақпаратты ойлап табуға болмайды. Тек searchResorts функциясынан алынған деректерді пайдаланыңыз.
                - Достық, кәсіби және пайдалы болыңыз.
                - Пайдаланушының сұрауы түсініксіз болса, нақтылау сұрақтарын қойыңыз.
                - Орынды болған жағдайда Алматы, Шымкент, Шымбұлақ және Алакөл сияқты танымал бағыттарды ұсыныңыз.
                - Ұсыныстарыңызда әрқашан курорттың атауын, қаласын, бағасын және негізгі қолайлылықтарды атаңыз.
                """
    );

    /**
     * Process user's message and return AI response with resort recommendations.
     *
     * @param userMessage User's query
     * @param language    Language code (en, ru, kk)
     * @return AI response with conversational text and resort recommendations
     */
    public AiChatResponse chat(String userMessage, String language) {
        log.info("Processing AI chat request - Language: {}, Message: {}", language, userMessage);

        // Get system prompt for the specified language
        String systemPrompt = SYSTEM_PROMPTS.getOrDefault(language, SYSTEM_PROMPTS.get("en"));

        // Track recommendations found via function calling
        List<ResortListResponse> recommendations = new ArrayList<>();

        // Create chat client with function calling (tool)
        ChatClient chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultFunction("searchResorts", searchResorts()) // Register the function
                .build();

        try {
            // Call AI with function calling enabled
            String aiReply = chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();

            log.info("AI response generated successfully");

            return AiChatResponse.builder()
                    .replyText(aiReply)
                    .recommendations(recommendations)
                    .build();

        } catch (Exception e) {
            log.error("Error processing AI chat request", e);

            // Return error message in appropriate language
            String errorMessage = switch (language) {
                case "ru" -> "Извините, произошла ошибка при обработке вашего запроса. Пожалуйста, попробуйте еще раз.";
                case "kk" -> "Кешіріңіз, сіздің сұрауыңызды өңдеу кезінде қате пайда болды. Қайталап көріңіз.";
                default -> "Sorry, there was an error processing your request. Please try again.";
            };

            return AiChatResponse.builder()
                    .replyText(errorMessage)
                    .recommendations(new ArrayList<>())
                    .build();
        }
    }

    /**
     * Search resorts function that AI can call via function calling.
     * This is automatically invoked by Spring AI when the AI decides to use this tool.
     *
     * @param request Search parameters from AI
     * @return List of matching resorts
     */
    public Function<ResortSearchRequest, List<ResortListResponse>> searchResorts() {
        return (request) -> {
            log.info("AI function called: searchResorts - Parameters: {}", request);

            try {
                // Convert AI request to repository filter
                ResortFilterRequest filter = ResortFilterRequest.builder()
                        .city(request.getCity())
                        .minPrice(request.getMinPrice())
                        .maxPrice(request.getMaxPrice())
                        .guests(request.getGuests())
                        .query(request.getQuery())
                        .amenities(request.getAmenities())
                        .build();

                // Query database
                Pageable pageable = PageRequest.of(0, MAX_RESULTS);
                Page<Resort> resorts = resortRepository.findAllWithDistance(filter, pageable);

                // Convert to response DTOs
                List<ResortListResponse> results = resorts.getContent().stream()
                        .map(resortMapper::toListResponse)
                        .toList();

                log.info("Found {} resorts matching criteria", results.size());
                return results;

            } catch (Exception e) {
                log.error("Error executing searchResorts function", e);
                return new ArrayList<>();
            }
        };
    }
}
