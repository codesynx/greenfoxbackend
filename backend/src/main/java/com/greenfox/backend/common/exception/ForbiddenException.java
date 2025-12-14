package com.greenfox.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when user lacks permission for an action.
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(message, "FORBIDDEN", HttpStatus.FORBIDDEN);
    }

    public ForbiddenException() {
        super("You don't have permission to perform this action", "FORBIDDEN", HttpStatus.FORBIDDEN);
    }
}

