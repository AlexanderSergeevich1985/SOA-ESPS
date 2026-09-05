package com.soaesps.notifications.service.template;

import com.soaesps.notifications.dto.InboundNotificationEvent;
import com.soaesps.notifications.repository.reactive.ReactiveNotificationTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Locale;

/**
 * Reactive template runtime engine supporting absolute hot-swapping of layout definitions.
 * Pulls textual configurations from PostgreSQL and layouts from MinIO by strict naming convention.
 */
@Service
public class ReactiveNamedTemplateEngine {
    /**
     * Container record for carrying both compiled title and body strings.
     */
    public record RenderedContent(String title, String body) {}

    private static final Logger log = LoggerFactory.getLogger(ReactiveNamedTemplateEngine.class);

    private final ReactiveNotificationTemplateRepository templateRepository;

    private final SpringTemplateEngine thymeleafEngine;

    public ReactiveNamedTemplateEngine(ReactiveNotificationTemplateRepository templateRepository,
                                       SpringTemplateEngine thymeleafEngine) {
        this.templateRepository = templateRepository;
        this.thymeleafEngine = thymeleafEngine;
    }

    /**
     * Resolves layout templates non-blockingly and compiles them using the native Thymeleaf engine.
     *
     * @param channelType Target messaging channel route (SMS, TELEGRAM, PUSH, EMAIL)
     * @param event       Inbound Kafka notification envelope tracking variables
     * @return A Mono emitting the fully rendered document layout text
     */
    public Mono<RenderedContent> renderAsync(String channelType, InboundNotificationEvent event) {
        String templateConventionKey = (event.notificationType() + "_" + channelType).toUpperCase();

        return templateRepository.findTemplateMeta(event.notificationType().toUpperCase(), channelType.toUpperCase())
                .flatMap(meta -> {
                    // Utility call: extract and safely build the dynamic target locale
                    Locale dynamicLocale = resolveLocale(event);

                    // Inject the resolved dynamic locale directly into the Thymeleaf context
                    Context context = new Context(dynamicLocale);
                    if (event.parameters() != null) {
                        context.setVariables(event.parameters());
                    }

                    // Compile the title dynamically using Thymeleaf inline text mode
                    String rawTitleTemplate = meta.inlineTitleTemplate() != null ? meta.inlineTitleTemplate() : "Notification Alert";
                    Mono<String> titleMono = Mono.fromCallable(() -> thymeleafEngine.process(rawTitleTemplate, context))
                            .subscribeOn(Schedulers.boundedElastic());

                    // Compile the body (from MinIO for Email, or from Postgres for text channels)
                    Mono<String> bodyMono;
                    if (meta.externalStorage()) {
                        String minioFileName = templateConventionKey + ".html";
                        bodyMono = fetchFromMinio(minioFileName)
                                .flatMap(rawHtmlTemplate -> Mono.fromCallable(() -> thymeleafEngine.process(rawHtmlTemplate, context))
                                        .subscribeOn(Schedulers.boundedElastic()));
                    } else {
                        if (meta.inlineTextTemplate() == null) {
                            return Mono.error(new IllegalStateException("Text template body is empty inside DB for key: " + templateConventionKey));
                        }
                        bodyMono = Mono.fromCallable(() -> thymeleafEngine.process(meta.inlineTextTemplate(), context))
                                .subscribeOn(Schedulers.boundedElastic());
                    }

                    // Zip both title and body rendering results together into the container
                    return Mono.zip(titleMono, bodyMono).map(tuple -> new RenderedContent(tuple.getT1(), tuple.getT2()));
                })
                .defaultIfEmpty(new RenderedContent("System Alert", "Notification update alert: event type " + event.notificationType() + " was processed."));
    }


    /**
     * Simulated asynchronous retrieval of heavy layout HTML payloads from MinIO storage.
     */
    private Mono<String> fetchFromMinio(String objectKey) {
        return Mono.fromCallable(() -> {
            log.debug("Cold-loading heavy email structure from MinIO using key: {}", objectKey);
            // Replace with real asynchronous object storage call: return minioClient.getObject("email-templates", objectKey);
            return "<html><body><h2>Transaction Update</h2><p>Hello {username}, transaction for amount {amount} completed.</p></body></html>";
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Evicts the entire compilation cache on demand.
     * Call this method via an administrative HTTP REST endpoint or Kafka management listener
     * to hot-swap templates at runtime without redeploying the application instance.
     */
    public void flushCache() {
        templateRepository.flushCache();
        log.warn("Notification configuration template cache was evicted successfully!");
    }

    /**
     * Extracts language tag criteria from the event parameters to safely compile a Locale instance.
     * Falls back to the configured fallback default locale if parameters are missing.
     *
     * @param event Inbound message container tracking payload metadata maps
     * @return Valid target Locale context
     */
    public static Locale resolveLocale(InboundNotificationEvent event) {
        // Check both common placeholder keys: "locale" and "language" (matching your interceptor parameter name)
        String localeTag = null;
        if (event != null && event.parameters() != null) {
            if (event.parameters().containsKey("locale")) {
                localeTag = event.parameters().get("locale").toString();
            } else if (event.parameters().containsKey("language")) {
                localeTag = event.parameters().get("language").toString();
            }
        }

        if (localeTag != null && !localeTag.isBlank()) {
            try {
                return Locale.forLanguageTag(localeTag);
            } catch (Exception ex) {
                log.warn("Failed to parse language tag [{}]. Falling back to configuration default.", localeTag, ex);
            }
        }

        // Dynamic fallback matching your InternationalConfig standard: Locale.US
        return Locale.US;
    }

}