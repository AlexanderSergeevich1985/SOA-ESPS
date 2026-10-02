package com.soaesps.jetdag.repository;

import com.soaesps.jetdag.entity.TaskEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.util.UUID;

@Repository
public interface TaskRepository extends ReactiveCrudRepository<TaskEntity, UUID> {

    /**
     * Streams all tasks associated with a specific pipeline configuration from PostgreSQL.
     * Fully non-blocking and reactive stream via R2DBC.
     *
     * @param pipelineId The unique UUID of the pipeline graph.
     * @return A reactive Flux emitting TaskEntity rows as they are fetched from the database network socket.
     */
    Flux<TaskEntity> findByPipelineId(UUID pipelineId);
}