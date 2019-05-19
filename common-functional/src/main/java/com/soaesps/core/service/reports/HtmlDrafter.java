package com.soaesps.core.service.reports;

public interface HtmlDrafter extends DocsDrafter {
    String getTemplatePath();

    void setTemplatePath(final String templatePath);
}