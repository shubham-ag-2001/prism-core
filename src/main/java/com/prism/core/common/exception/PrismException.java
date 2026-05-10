package com.prism.core.common.exception;

import org.springframework.http.HttpStatus;

public class PrismException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public PrismException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    // --- Convenience factory methods ---

    public static PrismException notFound(String message) {
        return new PrismException(message, HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND);
    }

    public static PrismException badRequest(String message) {
        return new PrismException(message, HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
    }

    public static PrismException badRequest(String message, String errorCode) {
        return new PrismException(message, HttpStatus.BAD_REQUEST, errorCode);
    }

    public static PrismException conflict(String message, String errorCode) {
        return new PrismException(message, HttpStatus.CONFLICT, errorCode);
    }

    public static PrismException unauthorized(String message) {
        return new PrismException(message, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
    }

    public static PrismException forbidden(String message) {
        return new PrismException(message, HttpStatus.FORBIDDEN, ErrorCode.FORBIDDEN);
    }

    public static PrismException internalError(String message) {
        return new PrismException(message, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR);
    }
}
