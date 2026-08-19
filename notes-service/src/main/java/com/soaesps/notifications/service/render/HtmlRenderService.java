package com.soaesps.notifications.service.render;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

@Service
public class HtmlRenderService {

    private final TemplateEngine templateEngine;

    public HtmlRenderService(@Qualifier("emailTemplateEngine") TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Renders any DTO into HTML using a Thymeleaf template from classpath.
     *
     * @param templateName template path relative to /templates (without .html)
     * @param dto          object exposed to the template under the given variable name
     * @param variableName name used in ${variableName.field} inside the template
     */
    public <T> String render(String templateName, T dto, String variableName) {
        Context context = new Context(Locale.ENGLISH);
        context.setVariable(variableName, dto);
        return templateEngine.process(templateName, context);
    }

    /** Convenience overload with multiple variables. */
    public String render(String templateName, Map<String, Object> variables) {
        Context context = new Context(Locale.ENGLISH);
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }
}