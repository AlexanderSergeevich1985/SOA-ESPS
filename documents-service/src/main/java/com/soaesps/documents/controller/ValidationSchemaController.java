package com.soaesps.documents.controller;

import com.soaesps.documents.service.ValidationSchemaService;
import com.soaesps.documents.validation.ValidationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * Administrative reactive REST controller managing dynamic polymorphic validation schemas.
 * Restricted strictly via mTLS identity propagation and role-based access control (RBAC).
 */
@RestController
@RequestMapping("/admin/validation-schemas")
@PreAuthorize("hasRole('ADMIN')") // Enforce endpoint protection strictly for corporate admins
public class ValidationSchemaController {

    private static final Logger log = LoggerFactory.getLogger(ValidationSchemaController.class);
    private static final String CACHE_NAME = "validation_schemas";

    private final ValidationSchemaService schemaService;
    private final CacheManager cacheManager;

    public ValidationSchemaController(ValidationSchemaService schemaService, CacheManager cacheManager) {
        this.schemaService = schemaService;
        this.cacheManager = cacheManager;
    }

    /**
     * Resolves the latest active validation schema blueprint for a specific document type.
     * Defaults strictly to the 'is_current: true' snapshot frame as advised by u/Denis-Hogberg.
     */
    @GetMapping("/{documentType}")
    public Mono<ValidationSchema> getCurrentSchema(
            @AuthenticationPrincipal String adminUserId,
            @PathVariable String documentType) {

        log.info("[Admin-Schema-API] Admin ID {} requested current active schema for type: {}", adminUserId, documentType);
        return schemaService.getSchema(documentType)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Active validation schema missing for type: " + documentType)));
    }

    /**
     * Resolves an explicit, superseded historical schema version for regression audit verifications.
     * Must be queried explicitly to safeguard hot-path data execution models.
     */
    @GetMapping("/{documentType}/versions/{version}")
    public Mono<ValidationSchema> getHistoricalSchema(
            @AuthenticationPrincipal String adminUserId,
            @PathVariable String documentType,
            @PathVariable int version) {

        log.info("[Admin-Schema-API] Admin ID {} requested EXPLICIT HISTORICAL schema for: {} v{}", adminUserId, documentType, version);
        return schemaService.getHistoricalSchema(documentType, version)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Historical schema snapshot absent for type %s version %d", documentType, version))));
    }

    /**
     * Persists or registers a fresh validation schema version parameters block into MongoDB collection bounds.
     * Automated programmatic hot-cache eviction triggers immediately upon successful completion.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ValidationSchema> createOrUpdateSchema(
            @AuthenticationPrincipal String adminUserId,
            @RequestBody ValidationSchema payload) {

        log.warn("[Admin-Schema-API] Admin ID {} is deploying/updating validation matrix blueprint for type: {}",
                adminUserId, payload.getDocumentType());
        return schemaService.saveSchema(payload);
    }

    /**
     * Permanently drops a specific schema configuration record from the core system maps.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> purgeSchemaById(
            @AuthenticationPrincipal String adminUserId,
            @PathVariable String id) {

        log.warn("[Admin-Schema-API] CRITICAL: Admin ID {} issued data expulsion request for schema object primary key ID: {}", adminUserId, id);
        return schemaService.deleteSchemaById(id);
    }

    /**
     * Emergency operations utility to force a complete programmatic flush of all hot cached memory partitions.
     * Used when MongoDB entries are modified manually outside the reactive execution scope boundaries.
     */
    @PostMapping("/cache/purge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> emergencyCachePurge(@AuthenticationPrincipal String adminUserId) {
        return Mono.fromRunnable(() -> {
            log.warn("[Admin-Schema-API] EMERGENCY OPERATION: Admin ID {} triggered manual global cache eviction block!", adminUserId);
            var cache = cacheManager.getCache(CACHE_NAME);
            if (cache != null) {
                cache.clear(); // Complete programmatic flush of the active Caffeine/Redis cache region bounds
                log.info("[Admin-Schema-API] Global validation schemas memory cache evicted successfully.");
            }

            var historicalCache = cacheManager.getCache("historical_schemas");
            if (historicalCache != null) {
                historicalCache.clear();
            }
        }).then();
    }
}