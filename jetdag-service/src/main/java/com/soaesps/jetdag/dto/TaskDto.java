package com.soaesps.jetdag.dto;

import java.util.UUID;

public record TaskDto(
        UUID id,
        String taskCode,
        String operatorType,
        String paramsJson,
        String fallbackTaskCode, // Points to another taskCode in DB to run if this one fails
        int timeoutSeconds,
        int retryAttempts,
        int retryBackoffSeconds
) {}