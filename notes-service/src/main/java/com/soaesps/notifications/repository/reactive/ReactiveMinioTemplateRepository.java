package com.soaesps.notifications.repository.reactive;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * MinIO-backed ultra-performance non-blocking implementation of {@link ReactiveTemplateRepository}.
 * Active when {@code storage.type=minio}. Uses MinioAsyncClient under the hood.
 */
@Repository
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
public class ReactiveMinioTemplateRepository implements ReactiveTemplateRepository {

    private static final Logger log = LoggerFactory.getLogger(ReactiveMinioTemplateRepository.class);
    private static final String NO_SUCH_KEY = "NoSuchKey";

    private final MinioAsyncClient minioAsyncClient; // Purely async/non-blocking driver engine bean
    private final String bucketName;

    public ReactiveMinioTemplateRepository(MinioAsyncClient minioAsyncClient,
                                           @Value("${minio.bucket-name:html-templates}") String bucketName) {
        this.minioAsyncClient = minioAsyncClient;
        this.bucketName = bucketName;
    }

    @Override
    @Cacheable(value = "minio_templates", key = "#templateName")
    public Mono<String> getTemplateContent(String templateName) {
        var args = GetObjectArgs.builder().bucket(bucketName).object(templateName).build();

        // Wrap the Future creation block to safely handle checked exceptions inside lambda definitions
        return Mono.defer(() -> {
                    try {
                        return Mono.fromFuture(minioAsyncClient.getObject(args));
                    } catch (Exception ex) {
                        return Mono.error(ex);
                    }
                })
                .flatMap(responseStream -> Mono.fromCallable(() -> {
                    try (InputStream is = responseStream) {
                        return org.springframework.util.StreamUtils.copyToString(is, java.nio.charset.StandardCharsets.UTF_8);
                    }
                }))
                .doOnNext(content -> log.debug("Non-blockingly downloaded template '{}' from MinIO", templateName))
                .onErrorMap(io.minio.errors.ErrorResponseException.class, ex -> {
                    if (NO_SUCH_KEY.equals(ex.errorResponse().code())) {
                        return new IllegalArgumentException("Template file not found in MinIO bucket bounds: " + templateName, ex);
                    }
                    return new IllegalStateException("MinIO asynchronous download tracking failure: " + templateName, ex);
                });
    }

    @Override
    public Mono<Boolean> exists(String templateName) {
        var args = StatObjectArgs.builder().bucket(bucketName).object(templateName).build();

        return Mono.defer(() -> {
                    try {
                        return Mono.fromFuture(minioAsyncClient.statObject(args));
                    } catch (Exception ex) {
                        return Mono.error(ex);
                    }
                })
                .map(stat -> true)
                .onErrorResume(ErrorResponseException.class, ex -> {
                    if (NO_SUCH_KEY.equals(ex.errorResponse().code())) {
                        return Mono.just(false);
                    }
                    return Mono.just(false);
                })
                .onErrorReturn(Exception.class, false);
    }

    @Override
    public Flux<String> listAll() {
        var args = ListObjectsArgs.builder().bucket(bucketName).build();

        // Since listObjects returns a blocking Iterable layout, we defer and offload it to background threads
        return Flux.defer(() -> {
                    try {
                        Iterable<Result<Item>> results = minioAsyncClient.listObjects(args);
                        List<String> objectKeys = new ArrayList<>();
                        for (Result<Item> result : results) {
                            objectKeys.add(result.get().objectName());
                        }
                        objectKeys.sort(String::compareTo);
                        return Flux.fromIterable(objectKeys);
                    } catch (Exception ex) {
                        return Flux.error(new IllegalStateException("Failed to parse async list response tokens", ex));
                    }
                })
                .subscribeOn(Schedulers.boundedElastic()); // Safe offloading to safeguard core Netty loop streams
    }

    @Override
    @CacheEvict(value = "minio_templates", key = "#templateName")
    public Mono<Void> saveTemplate(String templateName, String content) {
        return Mono.fromCallable(() -> content.getBytes(StandardCharsets.UTF_8))
                .flatMap(bytes -> {
                    var bais = new ByteArrayInputStream(bytes);
                    var args = PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(templateName)
                            .stream(bais, bytes.length, -1)
                            .contentType("text/html")
                            .build();

                    return Mono.defer(() -> {
                        try {
                            return Mono.fromFuture(minioAsyncClient.putObject(args));
                        } catch (Exception ex) {
                            return Mono.error(ex);
                        }
                    });
                })
                .doOnSuccess(v -> log.info("Successfully pushed HTML layout stream into MinIO object: {}", templateName))
                .then();
    }

    @Override
    @CacheEvict(value = "minio_templates", key = "#templateName")
    public Mono<Void> deleteTemplate(String templateName) {
        var args = RemoveObjectArgs.builder().bucket(bucketName).object(templateName).build();

        return Mono.defer(() -> {
                    try {
                        return Mono.fromFuture(minioAsyncClient.removeObject(args));
                    } catch (Exception ex) {
                        return Mono.error(ex);
                    }
                })
                .doOnSuccess(v -> log.info("Idempotent asynchronous removal completed for object: {}", templateName))
                .onErrorResume(ErrorResponseException.class, ex -> {
                    if (NO_SUCH_KEY.equals(ex.errorResponse().code())) {
                        return Mono.empty();
                    }
                    return Mono.error(ex);
                })
                .then();
    }

    /**
     * Explicit programmatic hook to drop the entire MinIO layout configuration cache domain bounds on demand.
     */
    @CacheEvict(value = "minio_templates", allEntries = true)
    public void flushCache() {
        log.warn("MinIO responsive HTML layout cache area was fully evicted!");
    }
}