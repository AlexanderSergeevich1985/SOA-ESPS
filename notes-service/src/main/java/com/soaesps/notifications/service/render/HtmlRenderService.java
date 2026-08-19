package com.soaesps.notifications.service.render;

import com.soaesps.notifications.exception.HtmlRenderException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateProcessingException;

import java.util.Collections;
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
     * Uses current thread locale from Spring Context.
     */
    public <T> String render(String templateName, T dto, String variableName) {
        return render(templateName, Collections.singletonMap(variableName, dto));
    }

    /**
     * Convenience overload with multiple variables.
     * Uses current thread locale from Spring Context.
     */
    public String render(String templateName, Map<String, Object> variables) {
        // Автоматически берет локаль из контекста (например, из Accept-Language контроллера)
        return render(templateName, variables, LocaleContextHolder.getLocale());
    }

    /**
     * Locale-aware rendering. Thymeleaf resolves localized templates automatically:
     * for Locale RU it first tries "invoice_ru.html", then falls back to "invoice.html".
     */
    public String render(String templateName, Map<String, Object> variables, Locale locale) {
        if (templateName == null || !templateName.matches("[a-zA-Z0-9/_-]+")) {
            throw new HtmlRenderException("Invalid template name: " + templateName);
        }

        Locale activeLocale = (locale != null) ? locale : LocaleContextHolder.getLocale();

        Context context = new Context(activeLocale);
        if (variables != null) {
            context.setVariables(variables);
        }

        try {
            return templateEngine.process(templateName, context);
        } catch (TemplateProcessingException ex) {
            throw new HtmlRenderException("Failed to render template: " + templateName, ex);
        }
    }
}
