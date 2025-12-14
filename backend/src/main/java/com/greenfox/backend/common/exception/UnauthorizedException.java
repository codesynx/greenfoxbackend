package com.greenfox.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication fails.
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }

    public UnauthorizedException() {
        super("Authentication required", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }
}

