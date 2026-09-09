package com.soaesps.documents.repository;

import com.soaesps.documents.validation.ValidationSchema;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ValidationSchemaRepository extends ReactiveMongoRepository<ValidationSchema, String> {
    /**
     * Resolves the system structural validation schema template rules profile.
     */
    Mono<ValidationSchema> findByDocumentType(String documentType);
}