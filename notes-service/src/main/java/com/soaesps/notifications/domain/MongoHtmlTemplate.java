package com.soaesps.notifications.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * MongoDB document holding a single HTML notification template.
 * The template name is used as the document _id so lookups are O(1) on the primary index.
 */
@Document(collection = "html_templates")
public class MongoHtmlTemplate {

    @Id
    private String id;        // template name, e.g. "notifications/otp"

    @Field("html_content")
    private String htmlContent;

    @Indexed
    @Field("updated_at")
    private Instant updatedAt;

    public MongoHtmlTemplate() {}

    public MongoHtmlTemplate(String id, String htmlContent) {
        this.id = id;
        this.htmlContent = htmlContent;
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getHtmlContent() { return htmlContent; }
    public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}