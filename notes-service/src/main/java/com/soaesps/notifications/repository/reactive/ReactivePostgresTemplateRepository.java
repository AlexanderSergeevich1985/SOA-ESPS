package com.soaesps.notifications.repository.reactive;

import com.soaesps.notifications.domain.reactive.NotificationTemplateRow;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * PostgreSQL-backed fully reactive implementation of {@link ReactiveTemplateRepository}.
 * Activated when {@code storage.type=postgres}. Maps logical file names to relational tables.
 */
@Repository
@ConditionalOnProperty(name = "storage.type", havingValue = "postgres")
public class ReactivePostgresTemplateRepository implements ReactiveTemplateRepository {

    private final ReactiveNotificationTemplateRepository r2dbcRepository;

    public ReactivePostgresTemplateRepository(ReactiveNotificationTemplateRepository r2dbcRepository) {
        this.r2dbcRepository = r2dbcRepository;
    }

    @Override
    public Mono<String> getTemplateContent(String templateName) {
        var keys = parseConventionKey(templateName);
        return r2dbcRepository.findTemplateMeta(keys.type(), keys.channel())
                .map(NotificationTemplateRow::inlineTextTemplate)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Template not found: " + templateName)));
    }

    @Override
    public Mono<Boolean> exists(String templateName) {
        var keys = parseConventionKey(templateName);
        return r2dbcRepository.findTemplateMeta(keys.type(), keys.channel())
                .map(row -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Flux<String> listAll() {
        return r2dbcRepository.findAll()
                .map(row -> (row.notificationType() + "_" + row.channelType() + ".txt").toUpperCase())
                .sort();
    }

    @Override
    public Mono<Void> saveTemplate(String templateName, String content) {
        var keys = parseConventionKey(templateName);
        var newRow = new NotificationTemplateRow(keys.type(), keys.channel(), false, content, "Notification Alert", null);
        return r2dbcRepository.save(newRow).then();
    }

    @Override
    public Mono<Void> deleteTemplate(String templateName) {
        var keys = parseConventionKey(templateName);
        return r2dbcRepository.findTemplateMeta(keys.type(), keys.channel())
                .flatMap(r2dbcRepository::delete);
    }

    private ParsedConventionKey parseConventionKey(String templateName) {
        // Extracts "PAYMENT_SUCCESS" and "SMS" from a logical filename like "PAYMENT_SUCCESS_SMS.txt"
        String cleanName = templateName.replace(".txt", "").replace(".html", "").toUpperCase();
        int lastUnderscore = cleanName.lastIndexOf("_");
        if (lastUnderscore == -1) {
            throw new IllegalArgumentException("Invalid naming convention format: " + templateName);
        }
        return new ParsedConventionKey(cleanName.substring(0, lastUnderscore), cleanName.substring(lastUnderscore + 1));
    }

    private record ParsedConventionKey(String type, String channel) {}

    /**
     * Evicts the hot cache metrics synchronized over the PostgreSQL relational engine mapping paths.
     * Overrides the fallback default interface token layer.
     */
    @Override
    public void flushCache() {
        // Delegates the eviction chain execution to the Spring Cache proxy wrapper configuration
        r2dbcRepository.flushCache();
    }
}
