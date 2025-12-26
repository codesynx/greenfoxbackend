package com.greenfox.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Configuration for Google Cloud credentials.
 * Supports credentials via GCP_SERVICE_ACCOUNT_JSON environment variable
 * for Digital Ocean and other cloud deployments.
 */
@Slf4j
@Configuration
public class GoogleCloudConfig {

    @Value("${app.gcp.service-account-json:}")
    private String serviceAccountJson;

    /**
     * Initialize Google Cloud credentials from environment variable.
     * This sets up Application Default Credentials for all Google Cloud services
     * including Vertex AI and Cloud Storage.
     */
    @PostConstruct
    public void setupCredentials() throws IOException {
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            try {
                // Write credentials to a temporary file
                Path tempCredentialsFile = Files.createTempFile("gcp-credentials-", ".json");
                Files.writeString(tempCredentialsFile, serviceAccountJson,
                        StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

                // Set GOOGLE_APPLICATION_CREDENTIALS environment variable
                // This is used by all Google Cloud SDKs including Vertex AI
                String credentialsPath = tempCredentialsFile.toAbsolutePath().toString();
                System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", credentialsPath);

                // Also set it as an environment variable for the current process
                // (Note: This only works for libraries that check system properties)
                log.info("Successfully configured Google Cloud credentials from GCP_SERVICE_ACCOUNT_JSON");
                log.info("Credentials file created at: {}", credentialsPath);

                // Mark file for deletion on JVM exit
                tempCredentialsFile.toFile().deleteOnExit();
            } catch (IOException e) {
                log.error("Failed to setup GCP credentials: {}", e.getMessage());
                throw new IOException("Invalid GCP_SERVICE_ACCOUNT_JSON format: " + e.getMessage(), e);
            }
        } else {
            String existingCredentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (existingCredentials != null) {
                log.info("Using GOOGLE_APPLICATION_CREDENTIALS from environment: {}", existingCredentials);
            } else {
                log.warn("No GCP credentials configured. GCP_SERVICE_ACCOUNT_JSON or GOOGLE_APPLICATION_CREDENTIALS not set.");
                log.warn("Google Cloud services (Vertex AI, Cloud Storage) may not work properly.");
            }
        }
    }

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            return GoogleCredentials.fromStream(
                    new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8)));
        } else {
            try {
                return GoogleCredentials.getApplicationDefault();
            } catch (IOException e) {
                log.error("Failed to load Google Cloud credentials: {}", e.getMessage());
                throw e;
            }
        }
    }
}
