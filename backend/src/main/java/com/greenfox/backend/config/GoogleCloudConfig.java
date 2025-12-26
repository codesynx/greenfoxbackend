package com.greenfox.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
     * Credentials are already configured in BackendApplication.main().
     * This bean is optional - if credentials are not available, the bean won't be created.
     */
    @Bean
    @ConditionalOnProperty(name = "app.gcp.enabled", matchIfMissing = true)
    public GoogleCredentials googleCredentials() {
        try {
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            log.info("Google Cloud credentials bean created successfully");
            return credentials;
        } catch (IOException e) {
            log.warn("Google Cloud credentials not available: {}", e.getMessage());
            log.warn("GCP services (Vertex AI, Cloud Storage) will not work. Set GCP_SERVICE_ACCOUNT_JSON environment variable.");
            return null;
        }
    }
}
