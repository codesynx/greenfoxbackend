package com.greenfox.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Configuration for Google Cloud credentials.
 * Credentials are set up early by GoogleCloudCredentialsInitializer.
 * This config just provides a GoogleCredentials bean for dependency injection.
 */
@Slf4j
@Configuration
public class GoogleCloudConfig {

    /**
     * Provide Google Cloud credentials bean.
     * Credentials are already configured by GoogleCloudCredentialsInitializer.
     */
    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            log.info("Google Cloud credentials bean created successfully");
            return credentials;
        } catch (IOException e) {
            log.error("Failed to load Google Cloud credentials: {}", e.getMessage());
            throw e;
        }
    }
}
