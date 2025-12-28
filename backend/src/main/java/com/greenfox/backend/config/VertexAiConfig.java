package com.greenfox.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.cloud.vertexai.VertexAI;

/**
 * Configuration for Google Vertex AI / Gemini.
 * Only initializes when GoogleCredentials are available.
 */
@Slf4j
@Configuration
public class VertexAiConfig {

    @Value("${spring.ai.vertex.ai.gemini.project-id:}")
    private String projectId;

    @Value("${spring.ai.vertex.ai.gemini.location:us-central1}")
    private String location;

    @Value("${spring.ai.vertex.ai.gemini.chat.options.model:gemini-1.5-flash}")
    private String model;

    /**
     * Create VertexAI client when GoogleCredentials bean is available.
     * Credentials are now hardcoded in GoogleCloudConfig, so this will always initialize.
     */
    @Bean
    @ConditionalOnBean(GoogleCredentials.class)
    public VertexAI vertexAI(GoogleCredentials credentials) {
        try {
            // Explicitly construct the API endpoint to ensure correct routing
            String apiEndpoint = location + "-aiplatform.googleapis.com:443";
            
            VertexAI.Builder builder = new VertexAI.Builder()
                    .setProjectId(projectId)
                    .setLocation(location)
                    .setApiEndpoint(apiEndpoint)
                    .setCredentials(credentials);
            
            VertexAI vertexAI = builder.build();
            log.info("✓ Vertex AI client initialized with credentials - Project: {}, Location: {}, Endpoint: {}", 
                    projectId, location, apiEndpoint);
            return vertexAI;
        } catch (Exception e) {
            log.error("Failed to initialize Vertex AI: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Vertex AI", e);
        }
    }

    /**
     * Create Gemini chat model when VertexAI client is available.
     */
    @Bean
    @ConditionalOnBean(VertexAI.class)
    public VertexAiGeminiChatModel vertexAiGeminiChatModel(VertexAI vertexAI) {
        VertexAiGeminiChatOptions options = VertexAiGeminiChatOptions.builder()
                .model(model)
                .temperature(0.7)
                .build();

        return new VertexAiGeminiChatModel(vertexAI, options);
    }

    /**
     * Create ChatClient.Builder when chat model is available.
     */
    @Bean
    @ConditionalOnBean(VertexAiGeminiChatModel.class)
    public ChatClient.Builder chatClientBuilder(VertexAiGeminiChatModel chatModel) {
        log.info("✓ ChatClient.Builder created with Vertex AI Gemini");
        return ChatClient.builder(chatModel);
    }
}
