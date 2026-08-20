package com.soaesps.notifications.exception;

/**
 * Thrown when a requested HTML template is not found in the configured storage.
 * Distinct from IllegalArgumentException so callers can react specifically
 * (e.g. fall back to a plain-text channel).
 */
public class TemplateNotFoundException extends RuntimeException {

    public TemplateNotFoundException(String templateName) {
        super("HTML template not found: " + templateName);
    }

    public TemplateNotFoundException(String templateName, Throwable cause) {
        super("HTML template not found: " + templateName, cause);
    }
}