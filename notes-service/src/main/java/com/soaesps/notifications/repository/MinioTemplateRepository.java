package com.soaesps.notifications.repository;

import com.soaesps.notifications.exception.TemplateNotFoundException;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MinIO-backed implementation of {@link TemplateRepository}.
 * Active when {@code storage.type=minio}.
 *
 * <p>Templates are stored as objects in a configurable bucket,
 * with the template name used as the object key.
 */
@Repository
@ConditionalOnProperty(name = "storage.type", havingValue = "minio")
public class MinioTemplateRepository implements TemplateRepository {

    private static final Logger log = LoggerFactory.getLogger(MinioTemplateRepository.class);
    private static final String NO_SUCH_KEY = "NoSuchKey";

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioTemplateRepository(MinioClient minioClient,
                                   @Value("${minio.bucket-name:html-templates}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @Override
    public String getTemplateContent(String templateName) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(templateName).build())) {
            String content = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            log.debug("Loaded HTML template '{}' from MinIO ({} bytes)", templateName, content.length());
            return content;
        } catch (ErrorResponseException e) {
            if (NO_SUCH_KEY.equals(e.errorResponse().code())) {
                throw new TemplateNotFoundException(templateName, e);
            }
            throw new IllegalStateException("MinIO error while loading template: " + templateName, e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to download HTML template from MinIO: " + templateName, e);
        }
    }

    /**
     * Checks existence without downloading the object body.
     * Uses MinIO HEAD request (statObject) which is cheaper than getObject.
     */
    @Override
    public boolean exists(String templateName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder().bucket(bucketName).object(templateName).build());
            return true;
        } catch (ErrorResponseException e) {
            if (NO_SUCH_KEY.equals(e.errorResponse().code())) {
                return false;
            }
            log.warn("MinIO error while checking existence of '{}'", templateName, e);
            return false;
        } catch (Exception e) {
            log.warn("Failed to check existence of '{}' in MinIO", templateName, e);
            return false;
        }
    }

    /**
     * Lists all template names stored in the bucket.
     * Returns only object keys (template names), not the HTML bodies.
     */
    @Override
    public List<String> listAll() {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).build());

            List<String> names = new ArrayList<>();
            for (Result<Item> result : results) {
                Item item = result.get();
                names.add(item.objectName());
            }
            names.sort(String::compareTo);
            log.debug("Listed {} templates from MinIO bucket '{}'", names.size(), bucketName);
            return names;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to list templates from MinIO", e);
        }
    }

    @Override
    public void saveTemplate(String templateName, String htmlContent) {
        try {
            byte[] bytes = htmlContent.getBytes(StandardCharsets.UTF_8);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(templateName)
                                .stream(bais, bytes.length, -1)
                                .contentType("text/html")
                                .build()
                );
            }
            log.info("Saved HTML template '{}' to MinIO ({} bytes)", templateName, bytes.length);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to upload HTML template to MinIO: " + templateName, e);
        }
    }

    /**
     * Idempotent delete: ignores NoSuchKey errors so deleting a non-existent
     * template is a silent no-op, matching the interface contract.
     */
    @Override
    public void deleteTemplate(String templateName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(templateName)
                            .build()
            );
            log.info("Deleted HTML template '{}' from MinIO", templateName);
        } catch (ErrorResponseException e) {
            if (NO_SUCH_KEY.equals(e.errorResponse().code())) {
                // Idempotent: object already gone, treat as success
                log.debug("Template '{}' already absent in MinIO, delete is no-op", templateName);
                return;
            }
            throw new IllegalStateException("Failed to delete HTML template from MinIO: " + templateName, e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to delete HTML template from MinIO: " + templateName, e);
        }
    }
}