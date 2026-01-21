package com.greenfox.backend.modules.ai.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.genai.Client;
import com.greenfox.backend.modules.ai.memory.ChatMemory;
import com.greenfox.backend.modules.ai.memory.InMemoryChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vertexai.embedding.VertexAiEmbeddingConnectionDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Configuration for Google Gen AI Client with Vertex AI backend.
 */
@Slf4j
@Configuration
public class GeminiClientConfig {

    @Value("${spring.ai.vertex.ai.gemini.project-id}")
    private String projectId;

    @Value("${spring.ai.vertex.ai.gemini.location}")
    private String location;

    @Value("${spring.ai.vertex.ai.gemini.chat.options.model}")
    private String modelName;

    @Value("${app.ai.system-prompt}")
    private String systemPrompt;

    /**
     * Create Google Gen AI Client configured for Vertex AI.
     * Uses Application Default Credentials (ADC) from the environment.
     */
    @Bean
    public Client geminiClient(GoogleCredentials credentials) {
        log.info("Initializing Google Gen AI Client for Vertex AI");
        log.info("Project: {}, Location: {}, Model: {}", projectId, location, modelName);

        Client client = Client.builder()
                .project(projectId)
                .location(location)
                .vertexAI(true)
                .credentials(credentials)
                .build();

        log.info("Google Gen AI Client initialized successfully");
        return client;
    }

    @Bean
    public String geminiModelName() {
        return modelName;
    }

    @Bean
    public String systemPrompt() {
        return systemPrompt;
    }

    /**
     * Bean for chat memory to store conversation history.
     */
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    /**
     * Provide connection details for Vertex AI Embeddings (Spring AI).
     * This allows Spring AI to use the hardcoded GoogleCredentials.
     */
    @Bean
    public VertexAiEmbeddingConnectionDetails vertexAiEmbeddingConnectionDetails(GoogleCredentials credentials) throws IOException {
        String endpoint = location + "-aiplatform.googleapis.com";
        
        PredictionServiceSettings settings = PredictionServiceSettings.newBuilder()
                .setEndpoint(endpoint)
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        return VertexAiEmbeddingConnectionDetails.builder()
                .projectId(projectId)
                .location(location)
                .predictionServiceSettings(settings)
                .build();
    }
}
