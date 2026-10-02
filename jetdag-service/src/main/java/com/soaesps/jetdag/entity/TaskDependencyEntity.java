package com.soaesps.jetdag.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.util.UUID;

@Table("task_dependencies")
public record TaskDependencyEntity(
        @Id Long id,
        UUID pipelineId,
        UUID upstreamTaskId,
        UUID downstreamTaskId
) {}