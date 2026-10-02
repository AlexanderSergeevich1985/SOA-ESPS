package com.soaesps.jetdag.repository;

import com.soaesps.jetdag.entity.TaskDependencyEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import java.util.UUID;

public interface TaskDependencyRepository extends ReactiveCrudRepository<TaskDependencyEntity, Long> {

    // Projection record to fetch flattened relationship data optimized for graph resolution
    record FlatDependencyProjection(
            UUID upstreamTaskId,
            UUID downstreamTaskId
    ) {}

    @Query("""
        SELECT upstream_task_id, downstream_task_id 
        FROM task_dependencies 
        WHERE pipeline_id = :pipelineId
    """)
    Flux<FlatDependencyProjection> findGraphEdges(UUID pipelineId);
}
