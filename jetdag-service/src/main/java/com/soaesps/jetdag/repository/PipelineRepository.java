package com.soaesps.jetdag.repository;

import com.soaesps.jetdag.entity.PipelineEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.util.UUID;

@Repository
public interface PipelineRepository extends ReactiveCrudRepository<PipelineEntity, UUID> {

    /**
     * Streams all active pipelines from PostgreSQL configuration tables.
     * Useful for dynamic initialization or dashboard management.
     *
     * @param isActive The status flag of the pipelines.
     * @return A reactive Flux emitting matching PipelineEntity configurations.
     */
    Flux<PipelineEntity> findByIsActive(boolean isActive);
}