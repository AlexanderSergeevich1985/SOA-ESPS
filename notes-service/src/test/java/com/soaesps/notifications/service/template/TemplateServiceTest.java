package com.soaesps.notifications.service.template;

import com.soaesps.notifications.exception.TemplateNotFoundException;
import com.soaesps.notifications.exception.InvalidTemplateException;
import com.soaesps.notifications.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private TemplateRepository repository;

    private TemplateService service;

    @BeforeEach
    void setUp() {
        // 1 KiB limit — small enough to trigger the guard in tests
        service = new TemplateService(repository, 1024);
    }

    @Nested
    @DisplayName("uploadTemplate")
    class UploadTests {

        @Test
        @DisplayName("Should delegate to repository when name and content are valid")
        void upload_shouldDelegateForValidInput() {
            service.uploadTemplate("notifications/otp", "<p>code: ${code}</p>");

            verify(repository).saveTemplate("notifications/otp", "<p>code: ${code}</p>");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "../evil", "/abs", "a b", "name with space"})
        @DisplayName("Should reject illegal template names")
        void upload_shouldRejectIllegalNames(String badName) {
            assertThatThrownBy(() -> service.uploadTemplate(badName, "<p>x</p>"))
                    .isInstanceOf(InvalidTemplateException.class);

            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("Should reject null name")
        void upload_shouldRejectNullName() {
            assertThatThrownBy(() -> service.uploadTemplate(null, "<p>x</p>"))
                    .isInstanceOf(InvalidTemplateException.class);
        }

        @Test
        @DisplayName("Should reject blank HTML content")
        void upload_shouldRejectBlankContent() {
            assertThatThrownBy(() -> service.uploadTemplate("otp", "   "))
                    .isInstanceOf(InvalidTemplateException.class);

            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("Should reject HTML that exceeds the configured size limit")
        void upload_shouldRejectOversizedContent() {
            String oversized = "x".repeat(2048);

            assertThatThrownBy(() -> service.uploadTemplate("otp", oversized))
                    .isInstanceOf(InvalidTemplateException.class)
                    .hasMessageContaining("1024");

            verifyNoInteractions(repository);
        }
    }

    @Nested
    @DisplayName("loadTemplate")
    class LoadTests {

        @Test
        @DisplayName("Should return content from repository")
        void load_shouldReturnRepositoryContent() {
            when(repository.getTemplateContent("otp")).thenReturn("<p>hi</p>");

            assertThat(service.loadTemplate("otp")).isEqualTo("<p>hi</p>");
        }

        @Test
        @DisplayName("Should propagate TemplateNotFoundException as-is")
        void load_shouldPropagateNotFound() {
            when(repository.getTemplateContent("missing"))
                    .thenThrow(new TemplateNotFoundException("missing"));

            assertThatThrownBy(() -> service.loadTemplate("missing"))
                    .isInstanceOf(TemplateNotFoundException.class);
        }

        @Test
        @DisplayName("Should validate name before touching the repository")
        void load_shouldValidateNameFirst() {
            assertThatThrownBy(() -> service.loadTemplate("../evil"))
                    .isInstanceOf(InvalidTemplateException.class);

            verifyNoInteractions(repository);
        }
    }

    @Nested
    @DisplayName("removeTemplate")
    class RemoveTests {

        @Test
        @DisplayName("Should delegate deletion to repository")
        void remove_shouldDelegate() {
            service.removeTemplate("otp");

            verify(repository).deleteTemplate("otp");
        }

        @Test
        @DisplayName("Should validate name before deletion")
        void remove_shouldValidateName() {
            assertThatThrownBy(() -> service.removeTemplate(""))
                    .isInstanceOf(InvalidTemplateException.class);

            verifyNoInteractions(repository);
        }
    }

    @Nested
    @DisplayName("exists")
    class ExistsTests {

        @Test
        @DisplayName("Should delegate to repository.exists")
        void exists_shouldDelegate() {
            when(repository.exists("otp")).thenReturn(true);

            assertThat(service.exists("otp")).isTrue();
        }
    }

    @Nested
    @DisplayName("listTemplates")
    class ListTests {

        @Test
        @DisplayName("Should return repository listing")
        void list_shouldReturnRepositoryList() {
            when(repository.listAll()).thenReturn(List.of("a", "b", "c"));

            assertThat(service.listTemplates()).containsExactly("a", "b", "c");
        }
    }
}