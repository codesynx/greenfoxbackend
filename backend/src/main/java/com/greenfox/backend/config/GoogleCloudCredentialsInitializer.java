package com.greenfox.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Initializes Google Cloud credentials before Spring beans are created.
 * This ensures credentials are available when Vertex AI client is initialized.
 */
@Slf4j
public class GoogleCloudCredentialsInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

        // Get credentials JSON from environment or property
        String credentialsJson = environment.getProperty("app.gcp.service-account-json");
        if (credentialsJson == null || credentialsJson.isBlank()) {
            credentialsJson = System.getenv("GCP_SERVICE_ACCOUNT_JSON");
        }

        if (credentialsJson != null && !credentialsJson.isBlank()) {
            try {
                // Write credentials to a temporary file
                Path tempCredentialsFile = Files.createTempFile("gcp-credentials-", ".json");
                Files.writeString(tempCredentialsFile, credentialsJson,
                        StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

                String credentialsPath = tempCredentialsFile.toAbsolutePath().toString();

                // Set environment variable using reflection hack
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
     * Hack to set environment variable at runtime.
     * Required because Google Cloud SDK reads environment variables.
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
        } catch (NoSuchFieldException e) {
            System.err.println("⚠ Could not set environment variable (field not found). Using file path only.");
        } catch (Exception e) {
            System.err.println("⚠ Could not set environment variable: " + e.getMessage());
        }
    }
}
