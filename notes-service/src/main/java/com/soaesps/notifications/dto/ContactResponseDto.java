package com.soaesps.notifications.dto;

/**
 * Standardized data transfer wrapper returned to the frontend boundaries.
 */
public record ContactResponseDto(
        Long id,
        String type,
        String status,
        String message
) {}