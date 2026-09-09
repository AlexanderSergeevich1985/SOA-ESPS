package com.soaesps.documents.service;

import com.soaesps.documents.repository.ValidationSchemaRepository;
import com.soaesps.documents.validation.ValidationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * High-performance service layer managing polymorphic data-driven validation schemas.
 * Integrates programmatic cache management directly into the non-blocking Project Reactor pipelines.
 */
@Service
public class ValidationSchemaService {

    private static final Logger log = LoggerFactory.getLogger(ValidationSchemaService.class);
    private static final String CACHE_NAME = "validation_schemas";

    private final ValidationSchemaRepository schemaRepository;
    private final CacheManager cacheManager;

    public ValidationSchemaService(ValidationSchemaRepository schemaRepository, CacheManager cacheManager) {
        this.schemaRepository = schemaRepository;
        this.cacheManager = cacheManager;
    }

    /**
     * Resolves the polymorphic rules configuration schema for a specific document type.
     * Uses Spring Cache layer underneath to achieve elite sub-millisecond hot-path validation speeds.
     *
     * @param documentType Dynamic type identifier descriptor (e.g., "CREDIT_DEAL", "PASSPORT")
     * @return Mono emitting the cached validation schema, or empty if it does not exist
     */
    @Cacheable(value = CACHE_NAME, key = "#documentType")
    public Mono<ValidationSchema> getSchema(String documentType) {
        String cleanType = documentType.toUpperCase();
        log.info("[Validation-Cache-Miss] Downloading fresh schema mapping from MongoDB for: {}", cleanType);

        return schemaRepository.findByDocumentType(cleanType)
                .cache(); // Memorizes the reactive pipeline stream signals inside the active JVM heap
    }

    /**
     * Persists or overrides a dynamic validation schema configuration inside MongoDB.
     * Automatically evicts the memory cache region upon successful reactive completion signal.
     *
     * @param schema Polymorphic layout config container carrying properties rules definitions
     * @return Mono emitting the newly persisted validation schema object record
     */
    public Mono<ValidationSchema> saveSchema(ValidationSchema schema) {
        if (schema == null || schema.getDocumentType() == null || schema.getDocumentType().isBlank()) {
            return Mono.error(new IllegalArgumentException("Cannot save invalid schema: Document type token cannot evaluate blank."));
        }

        String targetType = schema.getDocumentType().toUpperCase();
        schema.setDocumentType(targetType);

        log.info("[Validation-Schema-Save] Initiating save loop for schema type: {}", targetType);

        return schemaRepository.save(schema)
                .doOnSuccess(saved -> {
                    log.warn("[Validation-Cache-Evict] Evicting memory buffer cache for schema type due to state mutation: {}", targetType);
                    var cache = cacheManager.getCache(CACHE_NAME);
                    if (cache != null) {
                        cache.evict(targetType); // Programmatic thread-safe cache eviction flush execution
                    }
                })
                .doOnError(err -> log.error("[Validation-Schema-Save] Fatal database write crash on schema type: {}", targetType, err));
    }

    /**
     * Permanently purges a validation metadata schema block from the core collection cluster.
     * Idempotently flushes hot-cache memory addresses simultaneously.
     *
     * @param id MongoDB string metadata object primary key token identifier
     * @return Mono signaling completion of the eviction pipelines
     */
    public Mono<Void> deleteSchemaById(String id) {
        log.warn("[Validation-Schema-Delete] Request captured to drop schema ID: {}", id);

        return schemaRepository.findById(id)
                .flatMap(schema -> {
                    String type = schema.getDocumentType().toUpperCase();
                    return schemaRepository.delete(schema)
                            .doOnSuccess(v -> {
                                log.warn("[Validation-Cache-Evict] Idempotent cache flush completed for dropped type: {}", type);
                                var cache = cacheManager.getCache(CACHE_NAME);
                                if (cache != null) {
                                    cache.evict(type);
                                }
                            });
                });
    }
}