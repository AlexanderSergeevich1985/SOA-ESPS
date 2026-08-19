package com.soaesps.notifications.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;               // FIXED: spring5 -> spring6
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver; // FIXED: spring5 -> spring6
import org.thymeleaf.spring6.view.ThymeleafViewResolver;       // FIXED: spring5 -> spring6
import org.thymeleaf.templatemode.TemplateMode;

import java.nio.charset.StandardCharsets;

/**
 * Thymeleaf configuration for rendering HTML notification templates.
 * FIXED: Spring Boot 3 requires Thymeleaf 3.1+, which lives in org.thymeleaf.spring6.*;
 * the org.thymeleaf.spring5.* packages no longer exist.
 */
@Configuration
public class ThymeleafConfiguration {

    @Bean
    public SpringResourceTemplateResolver thymeleafTemplateResolver(ApplicationContext applicationContext) {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        // SpringResourceTemplateResolver is ApplicationContextAware; explicit injection
        // makes the dependency clear and guarantees resource resolution in any environment.
        templateResolver.setApplicationContext(applicationContext);
        // FIXED: explicit classpath prefix — the bare "/templates/html/" only worked
        // when resolved against the ServletContext in a deployed WAR.
        templateResolver.setPrefix("classpath:/templates/html/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        // FIXED: force UTF-8 so Cyrillic subjects/bodies are not mojibake'd in emails
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // Keep false for local dev; set true (or remove the line) in production for caching
        templateResolver.setCacheable(false);
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine(SpringResourceTemplateResolver thymeleafTemplateResolver) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(thymeleafTemplateResolver);
        // Compiles SpEL expressions for faster rendering
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    @Bean
    public ThymeleafViewResolver viewResolver(SpringTemplateEngine templateEngine) {
        final ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine);
        viewResolver.setOrder(1);
        viewResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        return viewResolver;
    }
}