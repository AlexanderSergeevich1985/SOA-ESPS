package com.soaesps.notifications.exception;

/**
 * Thrown when a notification template cannot be rendered.
 * Allows callers (channels, router) to react uniformly without knowing Thymeleaf internals.
 */
public class HtmlRenderException extends RuntimeException {

    public HtmlRenderException(String message) {
        super(message);
    }

    public HtmlRenderException(String message, Throwable cause) {
        super(message, cause);
    }
}