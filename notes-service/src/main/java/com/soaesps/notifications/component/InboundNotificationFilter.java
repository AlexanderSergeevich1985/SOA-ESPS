package com.soaesps.notifications.component;

import com.soaesps.notifications.dto.InboundNotificationEvent;
import com.soaesps.notifications.repository.reactive.ReactiveNotificationTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * High-performance inbound filter that validates if the incoming notification type
 * is supported by the system. Uses a hot in-memory cache synchronized with R2DBC database states.
 */
@Component
public class InboundNotificationFilter {

    private static final Logger log = LoggerFactory.getLogger(InboundNotificationFilter.class);

    private final ReactiveNotificationTypeRepository typeRepository;

    // AtomicReference holding an unmodifiable Set for lock-free read operations during high throughput
    private final AtomicReference<Set<String>> supportedTypesCache = new AtomicReference<>(Collections.emptySet());

    public InboundNotificationFilter(ReactiveNotificationTypeRepository typeRepository) {
        this.typeRepository = typeRepository;
    }

    /**
     * Initializes the supported type cache eagerly upon spring application context startup.
     */
    /*@PostConstruct
    public void init() {
        refreshSupportedTypes().block(); // Block once on startup to guarantee cache initialization before Kafka consumption starts
    }*/

    /**
     * Warmed up safely during framework context readiness avoiding schema initialization races.
     */
    /*@EventListener(ApplicationReadyEvent.class)
    public void initCacheOnStartup() {
        refreshSupportedTypes()
                .doOnSuccess(v -> log.info("Notification filtering cache safely warmed up."))
                .doOnError(err -> log.error("Initialization failure on type filter cache warm up: ", err))
                .subscribe(); // Non-blocking asynchronous initialization
    }*/

    /**
     * Spring Integration filter endpoint. Evaluates if the event payload notification type
     * is registered inside the hot system cache.
     *
     * @param event The typed inbound notification payload from Kafka
     * @return true if accepted downstream, false if routed to the discard channel
     */
    public boolean isNotificationTypeSupported(InboundNotificationEvent event) {
        if (event == null || event.notificationType() == null) {
            return false;
        }

        // Lazy initialization phase: if cache is null, safely pull data using block inside synchronous pipeline
        if (supportedTypesCache.get() == null) {
            synchronized (this) {
                if (supportedTypesCache.get() == null) {
                    log.info("Lazy-loading notification filter types dictionary upon first inbound message arrival.");
                    try {
                        refreshSupportedTypes().block();
                    } catch (Exception ex) {
                        log.error("Failed to lazily load active message types from database layer: ", ex);
                        // Fallback to empty bounds to prevent pipeline thread lockups
                        supportedTypesCache.compareAndSet(null, Collections.emptySet());
                    }
                }
            }
        }

        return supportedTypesCache.get().contains(event.notificationType().toUpperCase());
    }

    /**
     * Asynchronously triggers a database query to reload and overwrite the internal in-memory type cache.
     * This method can be safely invoked by an admin REST controller, an internal scheduler, or an eviction event listener.
     *
     * @return A Mono signaling completion of the reload phase
     */
    public Mono<Void> refreshSupportedTypes() {
        return typeRepository.findAllActiveTypes()
                .map(String::toUpperCase)
                .collect(ConcurrentHashMap::<String>newKeySet, Set::add)
                .doOnNext(freshTypes -> {
                    supportedTypesCache.set(Collections.unmodifiableSet(freshTypes));
                    log.info("Notification types cache successfully refreshed from DB. Active types count: {}", freshTypes.size());
                })
                .then();
    }
}