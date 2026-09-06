package com.soaesps.notifications.service.template;

import com.soaesps.notifications.exception.InvalidTemplateException;
import com.soaesps.notifications.repository.reactive.ReactiveTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

/**
 * Modernized reactive application-layer service for managing HTML and text notification templates.
 * Operates entirely on top of the non-blocking ReactiveTemplateRepository facade strategy.
 * Works seamlessly across any app server runtime architecture (Netty, Tomcat, Jetty, WildFly).
 */
@Service
public class ReactiveTemplateService {
    private static final Logger log = LoggerFactory.getLogger(ReactiveTemplateService.class);

    /**
     * Regex enforcing safe template names:
     *  - only letters, digits, '-', '_', '/'
     *  - no '..', no leading '/'
     * Prevents path-traversal and metadata data-injection attacks.
     */
    private static final Pattern VALID_NAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9/_-]{0,199}$");

    private final ReactiveTemplateRepository templateRepository;
    private final int maxHtmlSizeBytes;

    public ReactiveTemplateService(
            ReactiveTemplateRepository templateRepository,
            @Value("${notification.templates.max-size-bytes:524288}") int maxHtmlSizeBytes) {
        this.templateRepository = templateRepository;
        this.maxHtmlSizeBytes = maxHtmlSizeBytes;
    }

    /**
     * Uploads or overwrites a template in the dynamic active routing storage engine.
     * Automatically purges the runtime cache layer to enable instant hot-swapping.
     *
     * @param templateName safe logical convention name (e.g. "PAYMENT_SUCCESS_EMAIL.html")
     * @param htmlContent  full template layout string content
     * @return A Mono signaling completion of the write and eviction pipelines
     */
    public Mono<Void> uploadTemplate(String templateName, String htmlContent) {
        return Mono.fromRunnable(() -> {
                    validateName(templateName);
                    validateContent(htmlContent);
                })
                .then(Mono.defer(() -> {
                    log.info("Saving layout template structure '{}' ({} bytes)", templateName, htmlContent.length());
                    return templateRepository.saveTemplate(templateName, htmlContent);
                }))
                .doOnSuccess(v -> {
                    // Critical hot-swap step: Evicts cache domains instantly upon annual template mutation uploads
                    templateRepository.flushCache();
                    log.warn("Template configuration [{}] updated! Hot-swapping triggered across all caching regions.", templateName);
                });
    }

    /**
     * Loads the raw HTML or text source layout from active caching and storage layers.
     *
     * @return A Mono emitting the raw template layout content string
     */
    public Mono<String> loadTemplate(String templateName) {
        return Mono.fromRunnable(() -> validateName(templateName))
                .then(Mono.defer(() -> {
                    log.debug("Loading layout template content stream for: '{}'", templateName);
                    return templateRepository.getTemplateContent(templateName);
                }));
    }

    /**
     * Permanently removes a template descriptor from the system backend.
     * Operates as an idempotent silent no-op if the target key is already absent.
     */
    public Mono<Void> removeTemplate(String templateName) {
        return Mono.fromRunnable(() -> validateName(templateName))
                .then(Mono.defer(() -> {
                    log.info("Removing notification template key from storage engine: '{}'", templateName);
                    return templateRepository.deleteTemplate(templateName);
                }))
                .doOnSuccess(v -> {
                    // Evict cache to clean up memories
                    templateRepository.flushCache();
                });
    }

    /**
     * Whether a template with the given name currently exists in storage fields.
     */
    public Mono<Boolean> exists(String templateName) {
        return Mono.fromRunnable(() -> validateName(templateName))
                .then(Mono.defer(() -> templateRepository.exists(templateName)));
    }

    /**
     * Streams all template names currently stored for the administrative UI console views.
     */
    public Flux<String> listTemplates() {
        return templateRepository.listAll();
    }

    // ---------- private validation helpers ----------

    private void validateName(String templateName) {
        if (templateName == null || templateName.isBlank()) {
            throw new InvalidTemplateException("Template name must not be blank");
        }
        if (!VALID_NAME_PATTERN.matcher(templateName).matches()) {
            throw new InvalidTemplateException(
                    "Template name contains illegal characters or exceeds 200 chars bounds");
        }
    }

    private void validateContent(String htmlContent) {
        if (htmlContent == null || htmlContent.isBlank()) {
            throw new InvalidTemplateException("HTML content must not be blank");
        }
        if (htmlContent.length() > maxHtmlSizeBytes) {
            throw new InvalidTemplateException(
                    "HTML content exceeds maximum allowed size boundary of " + maxHtmlSizeBytes + " bytes");
        }
    }
}
