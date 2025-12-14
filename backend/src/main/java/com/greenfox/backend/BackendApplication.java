package com.greenfox.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * GreenFox Backend Application
 * Resort & Travel Booking Service for Kazakhstan
 */
@SpringBootApplication
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
