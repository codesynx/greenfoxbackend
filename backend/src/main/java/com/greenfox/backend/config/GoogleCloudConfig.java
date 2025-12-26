package com.greenfox.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Configuration for Google Cloud credentials.
 * Credentials are set up in BackendApplication.main() from GCP_SERVICE_ACCOUNT_JSON env var.
 * This config provides a GoogleCredentials bean for optional dependency injection.
 */
@Slf4j
@Configuration
public class GoogleCloudConfig {

    /**
     * Provide Google Cloud credentials bean.
     * Only created if GCP_SERVICE_ACCOUNT_JSON environment variable is set.
     */
    @Bean
    @ConditionalOnExpression("#{environment.getProperty('GCP_SERVICE_ACCOUNT_JSON') != null or environment.getProperty('GOOGLE_APPLICATION_CREDENTIALS') != null}")
    public GoogleCredentials googleCredentials() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            log.info("✓ Google Cloud credentials bean created successfully");
            return credentials;
        } catch (IOException e) {
            log.error("Failed to load Google Cloud credentials: {}", e.getMessage());
            throw new RuntimeException("Failed to load GCP credentials", e);
        }
    }
}
