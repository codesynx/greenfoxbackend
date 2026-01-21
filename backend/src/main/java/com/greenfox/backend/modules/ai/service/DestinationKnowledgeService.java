package com.greenfox.backend.modules.ai.service;

import com.greenfox.backend.modules.ai.rag.DestinationDocuments;
import com.greenfox.backend.modules.resort.entity.Resort;
import com.greenfox.backend.modules.resort.repository.ResortRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing destination knowledge in the vector store.
 * Handles initialization and retrieval of RAG documents.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DestinationKnowledgeService {

    private final VectorStore vectorStore;
    private final DestinationDocuments destinationDocuments;
    private final ResortRepository resortRepository;

    private static final int TOP_K_RESULTS = 5;
    private static final double SIMILARITY_THRESHOLD = 0.65;

    /**
     * Initialize the vector store with destination knowledge on application startup.
     */
    @PostConstruct
    @Transactional(readOnly = true)
    public void initializeKnowledge() {
        try {
            log.info("Initializing knowledge base in vector store...");

            // 1. Load static destination knowledge (cities, travel tips)
            List<Document> staticDocs = destinationDocuments.getAllDocuments();
            vectorStore.add(staticDocs);
            log.info("✓ Loaded {} static destination documents", staticDocs.size());

            // 2. Load dynamic resort knowledge from database
            List<Document> resortDocs = loadResortDocuments();
            if (!resortDocs.isEmpty()) {
                vectorStore.add(resortDocs);
                log.info("✓ Loaded {} resort documents from database", resortDocs.size());
            }

            log.info("Knowledge base initialization complete.");
        } catch (Exception e) {
            log.error("Failed to initialize knowledge base: {}", e.getMessage(), e);
            // Don't fail startup - the AI will still work, just without RAG context
        }
    }

    /**
     * Fetch all active resorts and convert them to RAG documents.
     */
    private List<Document> loadResortDocuments() {
        try {
            List<Resort> resorts = resortRepository.findByDeletedFalseAndActiveTrue();
            List<Document> documents = new ArrayList<>();

            for (Resort resort : resorts) {
                String content = formatResortContent(resort);
                Map<String, Object> metadata = Map.of(
                        "id", resort.getId().toString(),
                        "type", "resort",
                        "city", resort.getCity(),
                        "name", resort.getName()
                );
                documents.add(new Document(content, metadata));
            }

            return documents;
        } catch (Exception e) {
            log.error("Error loading resort documents: {}", e.getMessage());
            return List.of();
        }
    }

    private String formatResortContent(Resort resort) {
        String amenities = resort.getAmenities() != null ? String.join(", ", resort.getAmenities()) : "None listed";
        
        return String.format(
                "Resort: %s\n" +
                "Location: %s, %s\n" +
                "Type: %s\n" +
                "Price: %s KZT per night\n" +
                "Max Guests: %s\n" +
                "Description: %s\n" +
                "Amenities: %s\n",
                resort.getName(),
                resort.getCity(), resort.getAddress() != null ? resort.getAddress() : "",
                resort.getType(),
                resort.getBasePrice(),
                resort.getMaxGuests() != null ? resort.getMaxGuests() : "N/A",
                resort.getDescription(),
                amenities
        );
    }

    /**
     * Retrieve relevant destination context based on a user query.
     *
     * @param query The user's question or search query
     * @return List of relevant documents for context
     */
    public List<Document> retrieveContext(String query) {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(TOP_K_RESULTS)
                    .similarityThreshold(SIMILARITY_THRESHOLD)
                    .build();

            List<Document> results = vectorStore.similaritySearch(searchRequest);

            log.debug("RAG retrieval for query '{}' returned {} documents", query, results.size());
            return results;
        } catch (Exception e) {
            log.error("Failed to retrieve context for query '{}': {}", query, e.getMessage());
            return List.of();
        }
    }

    /**
     * Build a context string from retrieved documents.
     *
     * @param query The user's question
     * @return Formatted context string for the AI prompt
     */
    public String buildContextString(String query) {
        List<Document> documents = retrieveContext(query);

        if (documents.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        context.append("\n\nRelevant destination information:\n");

        for (int i = 0; i < documents.size(); i++) {
            context.append(String.format("[%d] %s\n", i + 1, documents.get(i).getText()));
        }

        return context.toString();
    }
}
