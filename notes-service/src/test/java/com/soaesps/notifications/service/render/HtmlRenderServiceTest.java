package com.soaesps.notifications.service.render;

import com.soaesps.notifications.exception.HtmlRenderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link HtmlRenderService}.
 * Uses a REAL Thymeleaf engine backed by test classpath templates —
 * rendering is pure logic, so mocking the engine would add no value.
 */
class HtmlRenderServiceTest {

    private HtmlRenderService service;

    /** Simple DTO exposed to templates as ${user.*} */
    record UserDto(String name, String email) {}

    /** Simple DTO exposed to templates as ${order.*} */
    record OrderDto(long id) {}

    @BeforeEach
    void setUp() {
        // Resolver reads templates from src/test/resources/templates/html/
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/html/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        service = new HtmlRenderService(engine);
    }

    @Test
    @DisplayName("Should render DTO fields into the template")
    void render_shouldRenderDtoIntoTemplate() {
        String html = service.render("greeting", new UserDto("Alice", "alice@example.com"), "user");

        assertThat(html)
                .contains("Alice")
                .contains("alice@example.com")
                .doesNotContain("guest"); // placeholder must be replaced
    }

    @Test
    @DisplayName("Should pick the localized template when it exists (greeting_ru.html)")
    void render_shouldUseLocalizedTemplateWhenAvailable() {
        String html = service.render(
                "greeting_ru",
                Map.of("user", new UserDto("Иван", "ivan@example.com")),
                Locale.forLanguageTag("ru"));

        assertThat(html).contains("Привет");
    }

    @Test
    @DisplayName("Should fall back to the default template when no localized version exists")
    void render_shouldFallbackToDefaultTemplate() {
        // order_ru.html does not exist -> Thymeleaf must fall back to order.html
        String html = service.render(
                "order",
                Map.of("order", new OrderDto(42L)),
                Locale.forLanguageTag("ru"));

        assertThat(html)
                .contains("Order")
                .contains("42");
    }

    @Test
    @DisplayName("Should render with multiple variables via the Map overload")
    void render_shouldSupportMultipleVariables() {
        String html = service.render("order", Map.of("order", new OrderDto(7L)));

        assertThat(html).contains("7");
    }

    @Test
    @DisplayName("Should reject template names with path traversal or special characters")
    void render_shouldRejectInvalidTemplateName() {
        UserDto dto = new UserDto("Alice", "alice@example.com");

        assertThatThrownBy(() -> service.render("../secrets", dto, "user"))
                .isInstanceOf(HtmlRenderException.class)
                .hasMessageContaining("Invalid template name");

        assertThatThrownBy(() -> service.render("greeting1${evil}", dto, "user"))
                .isInstanceOf(HtmlRenderException.class);
    }

    @Test
    @DisplayName("Should wrap Thymeleaf processing errors into HtmlRenderException")
    void render_shouldWrapTemplateProcessingErrors() {
        UserDto dto = new UserDto("Alice", "alice@example.com");

        // broken.html calls a non-existent method -> SpEL failure during processing
        assertThatThrownBy(() -> service.render("broken", dto, "user"))
                .isInstanceOf(HtmlRenderException.class)
                .hasMessageContaining("broken")
                .hasCauseInstanceOf(org.thymeleaf.exceptions.TemplateProcessingException.class);
    }

    @Test
    @DisplayName("Should wrap missing-template errors into HtmlRenderException")
    void render_shouldWrapMissingTemplateErrors() {
        assertThatThrownBy(() -> service.render("does_not_exist", Map.of()))
                .isInstanceOf(HtmlRenderException.class)
                .hasMessageContaining("does_not_exist");
    }
}