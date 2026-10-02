package com.soaesps.jetdag.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;
import java.util.UUID;

@Table("pipelines")
public record PipelineEntity(
        @Id UUID id,
        String name,
        String cronSchedule,
        boolean isActive,
        int maxConcurrentRuns,
        Instant createdAt,
        Instant updatedAt
) {
    // Factory method for creating new pipelines with default settings
    public static PipelineEntity createNew(String name, String cronSchedule) {
        return new PipelineEntity(null, name, cronSchedule, true, 1, Instant.now(), Instant.now());
    }
}