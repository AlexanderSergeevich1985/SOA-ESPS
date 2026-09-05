package com.soaesps.notifications.repository.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Purely non-blocking persistence abstraction for HTML and text notification templates.
 * Operates entirely within the Project Reactor ecosystem using Mono and Flux streams.
 */
public interface ReactiveTemplateRepository {

    /**
     * Asynchronously retrieves the raw text/HTML source layout identified by the template name.
     *
     * @param templateName The logical convention key, e.g., "PAYMENT_SUCCESS_EMAIL.html"
     * @return A Mono emitting the string content layout text
     */
    Mono<String> getTemplateContent(String templateName);

    /**
     * Checks whether a template configuration exists inside the targeted storage infrastructure.
     *
     * @param templateName The logical tracking layout filename key
     * @return A Mono emitting true if found, false otherwise
     */
    Mono<Boolean> exists(String templateName);

    /**
     * Streams all currently stored logical template identifier names sorted alphabetically.
     *
     * @return A Flux emitting template name strings sequentially
     */
    Flux<String> listAll();

    /**
     * Persists or updates template content layouts non-blockingly.
     *
     * @param templateName Logical convention tracking file key
     * @param content      Raw textual or structured HTML content, must not be blank
     * @return A Mono signaling completion of the persistence phase
     */
    Mono<Void> saveTemplate(String templateName, String content);

    /**
     * Idempotent reactive delete operation.
     *
     * @param templateName Target filename key to be deleted from metadata boundaries
     * @return A Mono signaling completion of the deletion pipeline execution
     */
    Mono<Void> deleteTemplate(String templateName);

    /**
     * Dynamic runtime hot-swap eviction hook to flush cache regions.
     */
    default void flushCache() {
        // Optional fallback: clear operation is a no-op if the active engine does not utilize caching bounds
    }
}