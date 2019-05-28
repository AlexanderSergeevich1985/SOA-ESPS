package com.soaesps.core.service.reports;

import com.soaesps.core.DataModels.BaseEntity;

public class BaseHtmlDrafter<T extends BaseEntity> implements HtmlDrafter {
    public String getFileName() {
        return "";
    }

    public void setFileName(final String fileName) {}

    public String getDocumentName() {
        return "";
    }

    public void setDocumentName(final String documentName) {}

    public String getTemplatePath() {
        return "";
    }

    public void setTemplatePath(final String templatePath) {}
}