package com.example.demo.infrastructure.common.exception;

/**
 * Exception thrown when content parsing fails
 */
public class ContentParseException extends RuntimeException {

    public ContentParseException(String message) {
        super(message);
    }

    public ContentParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
