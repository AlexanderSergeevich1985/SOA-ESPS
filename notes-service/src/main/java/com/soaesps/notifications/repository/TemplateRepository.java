package com.soaesps.notifications.repository;

import java.util.List;

/**
 * Persistence abstraction for HTML notification templates.
 * Implementations may be backed by MongoDB, MinIO, the local filesystem,
 * or any other store selected at runtime via the {@code storage.type} property.
 *
 * <p>Template names are logical keys (e.g. "emails/welcome.html").
 * Implementations are free to map them to documents, object keys or files.
 */
public interface TemplateRepository {

    /**
     * Returns the raw HTML source of the template identified by {@code templateName}.
     *
     * @param templateName logical key, e.g. "emails/welcome.html"
     * @return non-null HTML content
     * @throws TemplateNotFoundException if no template with that name is stored
     */
    String getTemplateContent(String templateName);

    /**
     * Checks whether a template with the given name is currently stored.
     *
     * <p>This method must be cheaper than {@link #getTemplateContent(String)} —
     * it should NOT load the HTML body into memory. Used for pre-flight validation
     * in the rendering pipeline and for existence checks in the admin API.
     *
     * @param templateName logical key
     * @return true if the template exists, false otherwise
     */
    boolean exists(String templateName);

    /**
     * Returns the names of all stored templates, sorted alphabetically.
     *
     * <p>Used by the admin UI to list available templates. Implementations should
     * project only the name/id field, not the HTML body, to keep this query cheap
     * even when thousands of templates are stored.
     *
     * @return immutable sorted list of template names; empty list if storage is empty
     */
    List<String> listAll();

    /**
     * Saves or updates HTML template content.
     *
     * <p>If a template with the same name already exists, its content is replaced.
     * If it does not exist, a new record is created. Implementations should
     * update a last-modified timestamp when present.
     *
     * @param templateName logical key, e.g. "emails/welcome.html"
     * @param htmlContent  the raw HTML string layout, must not be blank
     */
    void saveTemplate(String templateName, String htmlContent);

    /**
     * Deletes the HTML template identified by {@code templateName}.
     *
     * <p>This operation is idempotent: deleting a non-existent template
     * is a silent no-op and must NOT throw an exception.
     *
     * @param templateName logical key, e.g. "emails/welcome.html"
     */
    void deleteTemplate(String templateName);
}