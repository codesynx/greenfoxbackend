package com.greenfox.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for invalid request data.
 */
public class BadRequestException extends BusinessException {

    public BadRequestException(String message) {
        super(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message, Object details) {
        super(message, "BAD_REQUEST", HttpStatus.BAD_REQUEST, details);
    }
}

