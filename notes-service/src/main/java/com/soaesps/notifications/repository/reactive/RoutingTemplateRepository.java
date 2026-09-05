package com.soaesps.notifications.repository.reactive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Dynamic routing Facade implementing the Strategy pattern for template storage.
 * Automatically delegates execution paths to MinIO or PostgreSQL based on the file extension layout rules.
 */
@Primary // Tells Spring to inject this routing router implementation by default everywhere
@Repository
public class RoutingTemplateRepository implements ReactiveTemplateRepository {

    private static final Logger log = LoggerFactory.getLogger(RoutingTemplateRepository.class);

    private final ReactiveTemplateRepository postgresRepository;
    private final ReactiveTemplateRepository minioRepository;

    public RoutingTemplateRepository(
            @Qualifier("postgresTemplateRepository") ReactiveTemplateRepository postgresRepository,
            @Qualifier("minioTemplateRepository") ReactiveTemplateRepository minioRepository) {
        this.postgresRepository = postgresRepository;
        this.minioRepository = minioRepository;
    }

    @Override
    public Mono<String> getTemplateContent(String templateName) {
        return resolveStrategy(templateName).getTemplateContent(templateName);
    }

    @Override
    public Mono<Boolean> exists(String templateName) {
        return resolveStrategy(templateName).exists(templateName);
    }

    @Override
    public Flux<String> listAll() {
        // Combines lists from both database and object store ecosystems into a single stream
        return Flux.concat(postgresRepository.listAll(), minioRepository.listAll())
                .sort();
    }

    @Override
    public Mono<Void> saveTemplate(String templateName, String content) {
        return resolveStrategy(templateName).saveTemplate(templateName, content);
    }

    @Override
    public Mono<Void> deleteTemplate(String templateName) {
        return resolveStrategy(templateName).deleteTemplate(templateName);
    }

    /**
     * Internal evaluation engine selecting the active reactive storage strategy bean.
     *
     * @param templateName The logical layout name code (e.g., "PAYMENT_SUCCESS_EMAIL.html")
     * @return The concrete targeting ReactiveTemplateRepository implementation instance
     */
    private ReactiveTemplateRepository resolveStrategy(String templateName) {
        if (templateName != null && templateName.toLowerCase().endsWith(".html")) {
            log.debug("Routing template execution path [{}] to MinIO Object Storage strategy.", templateName);
            return minioRepository;
        }

        log.debug("Routing template execution path [{}] to PostgreSQL R2DBC Relational strategy.", templateName);
        return postgresRepository;
    }

    public void clearAllCaches() {
        postgresRepository.flushCache();
        minioRepository.flushCache();
    }
}
