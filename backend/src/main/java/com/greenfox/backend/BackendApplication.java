package com.greenfox.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * GreenFox Backend Application
 * Resort & Travel Booking Service for Kazakhstan
 */
@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class BackendApplication {

    public static void main(String[] args) {
        // Setup Google Cloud credentials BEFORE Spring starts
        setupGoogleCloudCredentials();

        SpringApplication.run(BackendApplication.class, args);
    }

    /**
     * Setup Google Cloud credentials from GCP_SERVICE_ACCOUNT_JSON environment variable.
     * This must run before Spring initializes to ensure Vertex AI can find credentials.
     */
    private static void setupGoogleCloudCredentials() {
        String credentialsJson = System.getenv("GCP_SERVICE_ACCOUNT_JSON");

        if (credentialsJson != null && !credentialsJson.isBlank()) {
            try {
                // Write credentials to a temporary file
                Path tempCredentialsFile = Files.createTempFile("gcp-credentials-", ".json");
                Files.writeString(tempCredentialsFile, credentialsJson,
                        StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

                String credentialsPath = tempCredentialsFile.toAbsolutePath().toString();

                // Set environment variable using reflection
                setEnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS", credentialsPath);

                System.out.println("✓ Google Cloud credentials configured from GCP_SERVICE_ACCOUNT_JSON");
                System.out.println("✓ Credentials file: " + credentialsPath);

                // Mark file for deletion on JVM exit
                tempCredentialsFile.toFile().deleteOnExit();
            } catch (IOException e) {
                System.err.println("✗ Failed to setup GCP credentials: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            String existingCredentials = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (existingCredentials != null) {
                System.out.println("✓ Using GOOGLE_APPLICATION_CREDENTIALS: " + existingCredentials);
            } else {
                System.out.println("⚠ Warning: No GCP credentials configured");
                System.out.println("  Set GCP_SERVICE_ACCOUNT_JSON or GOOGLE_APPLICATION_CREDENTIALS");
            }
        }
    }

    /**
     * Set environment variable at runtime using reflection.
     * Required because Google Cloud SDK reads from environment variables.
     */
    @SuppressWarnings("unchecked")
    private static void setEnvironmentVariable(String key, String value) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");

            java.lang.reflect.Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            java.util.Map<String, String> env = (java.util.Map<String, String>) theEnvironmentField.get(null);
            env.put(key, value);

            java.lang.reflect.Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            java.util.Map<String, String> cienv = (java.util.Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.put(key, value);

            System.out.println("✓ Set GOOGLE_APPLICATION_CREDENTIALS environment variable");
        } catch (Exception e) {
            System.err.println("⚠ Could not set environment variable via reflection: " + e.getMessage());
            System.err.println("  Some Google Cloud SDKs may still work via file path");
        }
    }
}
