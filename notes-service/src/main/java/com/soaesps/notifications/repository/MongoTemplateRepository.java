package com.soaesps.notifications.repository;

import com.soaesps.notifications.domain.MongoHtmlTemplate;
import com.soaesps.notifications.exception.TemplateNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB-backed implementation of {@link TemplateRepository}.
 * Active by default; can be switched off via {@code storage.type != mongo}.
 */
@Repository
@ConditionalOnProperty(name = "storage.type", havingValue = "mongo", matchIfMissing = true)
public class MongoTemplateRepository implements TemplateRepository {

    private static final Logger log = LoggerFactory.getLogger(MongoTemplateRepository.class);

    private final ActualMongoRepository actualMongoRepository;

    public MongoTemplateRepository(ActualMongoRepository actualMongoRepository) {
        this.actualMongoRepository = actualMongoRepository;
    }

    @Override
    public String getTemplateContent(String templateName) {
        return actualMongoRepository.findById(templateName)
                .map(MongoHtmlTemplate::getHtmlContent)
                // FIXED: domain exception instead of IllegalArgumentException,
                // so callers (render pipeline, channels) can react specifically
                .orElseThrow(() -> new TemplateNotFoundException(templateName));
    }

    /**
     * Existence check without loading the HTML body.
     * Backed by a cheap _id index lookup in MongoDB.
     */
    @Override
    public boolean exists(String templateName) {
        return actualMongoRepository.existsById(templateName);
    }

    /**
     * Lists all template names sorted alphabetically.
     * Uses the _id-only projection query — HTML bodies are NOT loaded.
     */
    @Override
    public List<String> listAll() {
        return actualMongoRepository.findAllIds().stream()
                .map(MongoHtmlTemplate::getId)
                .sorted()
                .toList();
    }

    @Override
    public void saveTemplate(String templateName, String htmlContent) {
        MongoHtmlTemplate template = new MongoHtmlTemplate();
        template.setId(templateName);
        template.setHtmlContent(htmlContent);

        actualMongoRepository.save(template);
        log.info("Saved HTML template '{}' ({} bytes)", templateName, htmlContent.length());
    }

    /**
     * Idempotent delete: MongoRepository.deleteById is a silent no-op
     * for a missing id, which matches the interface contract.
     */
    @Override
    public void deleteTemplate(String templateName) {
        actualMongoRepository.deleteById(templateName);
        log.info("Deleted HTML template '{}'", templateName);
    }
}