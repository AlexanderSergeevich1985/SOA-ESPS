package com.soaesps.notifications.exception;

/**
 * Thrown when a template operation receives invalid input.
 * Distinct from {@link IllegalArgumentException} so REST controllers can map it
 * to a dedicated HTTP 400 response body without leaking internals.
 */
public class InvalidTemplateException extends RuntimeException {
    public InvalidTemplateException(String message) {
        super(message);
    }
}