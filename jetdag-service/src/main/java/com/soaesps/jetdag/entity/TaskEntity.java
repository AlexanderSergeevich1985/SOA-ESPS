package com.soaesps.jetdag.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("tasks")
public record TaskEntity(
        @Id UUID id,
        UUID pipelineId,
        String taskCode,
        String operatorType,
        String paramsJson,
        String fallbackTaskCode,
        int timeoutSeconds,
        int retryAttempts,
        int retryBackoffSeconds
) {}