package com.greenfox.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for business logic errors.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;
    private final Object details;

    public BusinessException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = null;
    }

    public BusinessException(String message, String errorCode, HttpStatus httpStatus, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details;
    }
}

