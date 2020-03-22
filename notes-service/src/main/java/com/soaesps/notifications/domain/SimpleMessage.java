package com.soaesps.notifications.domain;

import java.util.Map;

public class SimpleMessage {
    private MessageType type;

    private String templateName;

    private Map<String, String> templateData;

    public MessageType getType() {
        return type;
    }

    public void setType(final MessageType type) {
        this.type = type;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(final String templateName) {
        this.templateName = templateName;
    }

    public Map<String, String> getTemplateData() {
        return templateData;
    }

    public void setTemplateData(final Map<String, String> templateData) {
        this.templateData = templateData;
    }
}