package com.soaesps.notifications.service.template;

import com.soaesps.notifications.exception.InvalidTemplateException;
import com.soaesps.notifications.exception.TemplateNotFoundException;
import com.soaesps.notifications.repository.TemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Application-layer service for managing HTML notification templates.
 * Delegates all persistence to the active {@link TemplateRepository} implementation
 * (Mongo/MinIO/filesystem), selected at runtime via {@code storage.type}.
 *
 * Responsibility:
 *  - input validation (guards against malformed names / oversized payloads);
 *  - logging of every write operation (audit requirement for banking templates);
 *  - translation of persistence errors into domain-level exceptions.
 */
@Service
public class TemplateService {

    private static final Logger log = LoggerFactory.getLogger(TemplateService.class);

    /**
     * Regex enforcing safe template names:
     *  - only letters, digits, '-', '_', '/'
     *  - no '..', no leading '/'
     * Prevents path-traversal and Mongo document-id injection.
     */
    private static final Pattern VALID_NAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9/_-]{0,199}$");

    private final TemplateRepository templateRepository;
    private final int maxHtmlSizeBytes;

    public TemplateService(
            TemplateRepository templateRepository,
            @Value("${notification.templates.max-size-bytes:524288}") int maxHtmlSizeBytes) {
        this.templateRepository = templateRepository;
        this.maxHtmlSizeBytes = maxHtmlSizeBytes;
    }

    /**
     * Uploads or overwrites a template in the active storage engine.
     *
     * @param templateName safe logical name (e.g. "notifications/otp")
     * @param htmlContent  full HTML body, must not exceed {@code notification.templates.max-size-bytes}
     * @throws InvalidTemplateException on blank name, blank content, illegal characters or oversize payload
     */
    public void uploadTemplate(String templateName, String htmlContent) {
        validateName(templateName);
        validateContent(htmlContent);

        log.info("Saving HTML template '{}' ({} bytes)", templateName, htmlContent.length());
        templateRepository.saveTemplate(templateName, htmlContent);
    }

    /**
     * Loads the raw HTML source of a template from active storage.
     * Results are cached at the repository layer, so this call is cheap on hot paths.
     *
     * @throws InvalidTemplateException  on blank or malformed name
     * @throws TemplateNotFoundException if the template does not exist
     */
    public String loadTemplate(String templateName) {
        validateName(templateName);

        log.debug("Loading HTML template '{}'", templateName);
        return templateRepository.getTemplateContent(templateName);
    }

    /**
     * Permanently removes a template from active storage.
     * A no-op (no exception) if the template did not exist — idempotent semantics.
     */
    public void removeTemplate(String templateName) {
        validateName(templateName);

        log.info("Removing HTML template '{}'", templateName);
        templateRepository.deleteTemplate(templateName);
    }

    /** Whether a template with the given name currently exists in storage. */
    public boolean exists(String templateName) {
        validateName(templateName);
        return templateRepository.exists(templateName);
    }

    /** Lists all template names currently stored (for admin UI). */
    public List<String> listTemplates() {
        // Implementation note: this is a pass-through; the repository is responsible
        // for an efficient listing strategy (e.g. Mongo projection of _id only).
        return templateRepository.listAll();
    }

    // ---------- private validation helpers ----------

    private void validateName(String templateName) {
        if (templateName == null || templateName.isBlank()) {
            throw new InvalidTemplateException("Template name must not be blank");
        }
        if (!VALID_NAME_PATTERN.matcher(templateName).matches()) {
            // Avoid logging the raw name to prevent log-injection attacks
            throw new InvalidTemplateException(
                    "Template name contains illegal characters or exceeds 200 chars");
        }
    }

    private void validateContent(String htmlContent) {
        if (htmlContent == null || htmlContent.isBlank()) {
            throw new InvalidTemplateException("HTML content must not be blank");
        }
        if (htmlContent.length() > maxHtmlSizeBytes) {
            throw new InvalidTemplateException(
                    "HTML content exceeds maximum allowed size of " + maxHtmlSizeBytes + " bytes");
        }
    }
}