package com.greenfox.backend.modules.ai.service;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Tool;
import com.greenfox.backend.modules.ai.dto.ChatRequest;
import com.greenfox.backend.modules.ai.dto.ChatResponse;
import com.greenfox.backend.modules.ai.dto.ResortRecommendation;
import com.greenfox.backend.modules.ai.memory.AssistantMessage;
import com.greenfox.backend.modules.ai.memory.ChatMemory;
import com.greenfox.backend.modules.ai.memory.Message;
import com.greenfox.backend.modules.ai.memory.UserMessage;
import com.greenfox.backend.modules.ai.tools.ResortSearchTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.greenfox.backend.modules.resort.service.ResortService;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for handling AI chat interactions using Google Gen AI SDK.
 * Uses automatic function calling for resort search capabilities.
 */
@Slf4j
@Service
public class AiChatService {

    private final Client geminiClient;
    private final String modelName;
    private final String enhancedSystemPrompt; // Enhanced with cities list
    private final DestinationKnowledgeService destinationKnowledgeService;
    private final Method searchResortsMethod;
    private final Method getResortDetailsMethod;
    private final ChatMemory chatMemory;

    public AiChatService(
            Client geminiClient,
            @Qualifier("geminiModelName") String modelName,
            @Qualifier("systemPrompt") String systemPrompt,
            DestinationKnowledgeService destinationKnowledgeService,
            ChatMemory chatMemory,
            ResortService resortService
    ) throws NoSuchMethodException {
        this.geminiClient = geminiClient;
        this.modelName = modelName;
        this.destinationKnowledgeService = destinationKnowledgeService;
        this.chatMemory = chatMemory;

        // Load cities at startup and enhance system prompt
        List<String> cities = resortService.getAllCities();
        String citiesList = cities.isEmpty() ? "No cities available yet" : String.join(", ", cities);
        this.enhancedSystemPrompt = systemPrompt + "\n\nAVAILABLE DESTINATIONS IN DATABASE: " + citiesList +
                ". Use this list directly - do NOT call getAllCities().";
        log.info("Loaded {} cities into system prompt: {}", cities.size(), citiesList);

        // Load tools for automatic function calling (removed getAllCities - now in prompt)
        this.searchResortsMethod = ResortSearchTool.class.getMethod(
                "searchResorts",
                String.class,  // city
                String.class,  // minPrice
                String.class,  // maxPrice
                String.class,  // guests
                String.class   // query
        );

        this.getResortDetailsMethod = ResortSearchTool.class.getMethod(
                "getResortDetails",
                String.class // id
        );

        log.info("AiChatService initialized with model: {}", modelName);
    }

    /**
     * Process a chat message and return AI response with optional resort recommendations.
     *
     * @param request The chat request containing the user message
     * @return ChatResponse with AI reply and any resort recommendations
     */
    public ChatResponse chat(ChatRequest request) {
        log.info("Processing AI chat request: {}", request.getMessage());

        // Resolve Session ID
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = UUID.randomUUID().toString();
            log.info("Created new session ID: {}", sessionId);
        }

        // Clear previous search results
        ResortSearchTool.clearResults();

        try {
            // Retrieve history FIRST to detect follow-ups
            List<Message> history = chatMemory.get(sessionId, 10); // Reduced from 20 to 10 for performance

            // Detect if this is a follow-up question (skip RAG for follow-ups)
            boolean isFollowUp = isFollowUpQuestion(history, request.getMessage());

            String enhancedMessageText = request.getMessage();

            // Only build RAG context for NEW queries, not follow-ups
            if (!isFollowUp) {
                String ragContext = destinationKnowledgeService.buildContextString(request.getMessage());
                if (!ragContext.isEmpty()) {
                    enhancedMessageText = request.getMessage() + "\n\nContext:\n" + ragContext;
                    log.debug("Enhanced message with RAG context");
                }
            } else {
                log.debug("Skipping RAG for follow-up question: {}", request.getMessage());
            }

            // Build content list (History + New Message)
            List<Content> contents = new ArrayList<>();

            // Add history
            for (Message msg : history) {
                String role = (msg instanceof UserMessage) ? "user" : "model";
                contents.add(Content.builder()
                        .role(role)
                        .parts(List.of(Part.fromText(msg.getContent())))
                        .build());
            }

            // Add current message
            contents.add(Content.builder()
                    .role("user")
                    .parts(List.of(Part.fromText(enhancedMessageText)))
                    .build());

            // Build system instruction with enhanced prompt (includes cities)
            Content systemInstruction = Content.fromParts(Part.fromText(enhancedSystemPrompt));

            // Configure with system prompt and function calling tools (removed getAllCities - now in prompt)
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(systemInstruction)
                    .temperature(0.7f)
                    .maxOutputTokens(2048)
                    .tools(Tool.builder().functions(List.of(searchResortsMethod, getResortDetailsMethod)))
                    .build();

            // Call Gemini with history
            GenerateContentResponse response = geminiClient.models.generateContent(
                    modelName,
                    contents,
                    config
            );

            String responseText = response.text();

            // Log function calling history if available
            response.automaticFunctionCallingHistory().ifPresent(historyLogs ->
                    log.debug("Function calling history: {}", historyLogs));

            // Retrieve recommendations captured during tool execution
            List<ResortRecommendation> recommendations = ResortSearchTool.getLastSearchResults();

            log.info("AI chat completed. Search performed: {}, Recommendations: {}",
                    !recommendations.isEmpty(), recommendations.size());

            // Save to memory
            chatMemory.add(sessionId, new UserMessage(request.getMessage())); // Save original user message, not enhanced

            // Append search results summary to assistant memory so LLM remembers what was found
            String assistantContent = responseText;
            if (!recommendations.isEmpty()) {
                String resultSummary = recommendations.stream()
                        .map(r -> String.format("- %s (ID: %s, City: %s, Price: %s KZT)",
                                r.getName(), r.getId(), r.getCity(), r.getBasePrice()))
                        .collect(Collectors.joining("\n"));
                assistantContent = responseText + "\n\n[SEARCH RESULTS FOR REFERENCE:\n" + resultSummary + "]";
            }
            chatMemory.add(sessionId, new AssistantMessage(assistantContent));

            return ChatResponse.builder()
                    .replyText(responseText)
                    .sessionId(sessionId)
                    .recommendations(recommendations.isEmpty() ? null : recommendations)
                    .searchPerformed(!recommendations.isEmpty())
                    .build();

        } catch (Exception e) {
            log.error("Error processing AI chat request: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .replyText("I apologize, but I'm experiencing technical difficulties. Please try again later.")
                    .sessionId(sessionId)
                    .recommendations(null)
                    .searchPerformed(false)
                    .build();
        } finally {
            ResortSearchTool.clearResults();
        }
    }

    /**
     * Detect if a message is a follow-up question (doesn't need RAG context).
     * Follow-ups are short messages that reference previous conversation context.
     */
    private boolean isFollowUpQuestion(List<Message> history, String message) {
        // No history = not a follow-up
        if (history.isEmpty()) {
            return false;
        }

        String lowerMessage = message.toLowerCase().trim();

        // Short messages in an ongoing conversation are likely follow-ups
        if (message.length() < 60) {
            return true;
        }

        // Common follow-up patterns
        String[] followUpPatterns = {
                "tell me more", "more info", "more details", "what about",
                "which one", "the first", "the second", "the third", "the last",
                "cheapest", "most expensive", "highest rated", "best",
                "how much", "what's the price", "price of",
                "can you", "could you", "please",
                "yes", "no", "ok", "okay", "sure", "thanks", "thank you",
                "book", "reserve", "availability"
        };

        for (String pattern : followUpPatterns) {
            if (lowerMessage.contains(pattern)) {
                return true;
            }
        }

        return false;
    }
}
