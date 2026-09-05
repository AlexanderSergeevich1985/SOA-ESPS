package com.soaesps.notifications.repository.reactive;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
@DisplayName("Reactive MinIO Template Repository Unit Test")
class ReactiveMinioTemplateRepositoryTest {

    @Mock
    private MinioAsyncClient minioAsyncClient;

    private ReactiveMinioTemplateRepository repository;
    private final String testBucket = "test-html-templates";

    @BeforeEach
    void setUp() {
        repository = new ReactiveMinioTemplateRepository(minioAsyncClient, testBucket);
    }

    @Test
    @DisplayName("Should successfully download raw HTML layout content non-blockingly from MinIO")
    void shouldGetTemplateContentSuccessfully() throws Exception {
        // Arrange
        String templateName = "PAYMENT_SUCCESS_EMAIL.html";
        String expectedContent = "<html><body><h2>Hello [[${username}]]</h2></body></html>";
        byte[] contentBytes = expectedContent.getBytes(StandardCharsets.UTF_8);

        CompletableFuture<GetObjectResponse> future = CompletableFuture.completedFuture(
                new GetObjectResponse(null, testBucket, null, templateName, new ByteArrayInputStream(contentBytes))
        );

        Mockito.when(minioAsyncClient.getObject(Mockito.any(GetObjectArgs.class)))
                .thenReturn(future);

        // Act
        Mono<String> contentMono = repository.getTemplateContent(templateName);

        // Assert
        StepVerifier.create(contentMono)
                .expectNext(expectedContent)
                .verifyComplete();

        Mockito.verify(minioAsyncClient, Mockito.times(1)).getObject(Mockito.any(GetObjectArgs.class));
    }

    @Test
    @DisplayName("Should translate NoSuchKey error response exception into dynamic IllegalArgumentException signal")
    void shouldMapNoSuchKeyExceptionToIllegalArgumentException() throws Exception {
        // Arrange
        String missingKey = "ABSENT_LAYOUT.html";

        ErrorResponse errorResponse = Mockito.mock(ErrorResponse.class);
        Mockito.when(errorResponse.code()).thenReturn("NoSuchKey");

        ErrorResponseException minioEx = new ErrorResponseException(errorResponse, null, "Object absent");

        CompletableFuture<GetObjectResponse> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(minioEx);

        Mockito.when(minioAsyncClient.getObject(Mockito.any(GetObjectArgs.class)))
                .thenReturn(failedFuture);

        // Act
        Mono<String> contentMono = repository.getTemplateContent(missingKey);

        // Assert
        StepVerifier.create(contentMono)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    @DisplayName("Should evaluate true mono signal if target metadata file object exists inside bucket parameters")
    void shouldReturnTrueWhenTemplateExistsInStorage() throws Exception {
        // Arrange
        String templateName = "SECURITY_ALERT_EMAIL.html";

        StatObjectResponse mockStat = Mockito.mock(StatObjectResponse.class);
        CompletableFuture<StatObjectResponse> future = CompletableFuture.completedFuture(mockStat);

        Mockito.when(minioAsyncClient.statObject(Mockito.any(StatObjectArgs.class)))
                .thenReturn(future);

        // Act
        Mono<Boolean> existsMono = repository.exists(templateName);

        // Assert
        StepVerifier.create(existsMono)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should successfully upload string payload structures downstream to MinIO container layer")
    void shouldSaveTemplateLayoutSuccessfully() throws Exception {
        // Arrange
        String templateName = "PROMO_EMAIL.html";
        String htmlLayout = "<h1>Discount attached</h1>";

        ObjectWriteResponse mockWriteResponse = Mockito.mock(ObjectWriteResponse.class);
        CompletableFuture<ObjectWriteResponse> future = CompletableFuture.completedFuture(mockWriteResponse);

        Mockito.when(minioAsyncClient.putObject(Mockito.any(PutObjectArgs.class)))
                .thenReturn(future);

        // Act
        Mono<Void> saveMono = repository.saveTemplate(templateName, htmlLayout);

        // Assert
        StepVerifier.create(saveMono)
                .verifyComplete();
    }
}