package com.soaesps.documents.repository;

import com.soaesps.documents.validation.ValidationSchema;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ValidationSchemaRepository extends ReactiveMongoRepository<ValidationSchema, String> {
    // Used for active creation routing paths
    @Query("{ 'document_type': ?0, 'is_current': true }")
    Mono<ValidationSchema> findCurrentSchema(String documentType);

    // Used strictly for historical auditing tasks verification loops
    @Query("{ 'document_type': ?0, 'version': ?1 }")
    Mono<ValidationSchema> findSchemaByVersion(String documentType, int version);
}