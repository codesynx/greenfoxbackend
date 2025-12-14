package com.greenfox.backend.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT configuration properties.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
}

