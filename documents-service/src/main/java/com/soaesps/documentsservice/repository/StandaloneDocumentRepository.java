package com.soaesps.documentsservice.repository;

import com.soaesps.documentsservice.DataModels.BaseDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Handles lifecycle tasks strictly for standalone individual documents.
 */
@Repository
public interface StandaloneDocumentRepository extends ReactiveMongoRepository<BaseDocument, String> {

    Mono<BaseDocument> findByDomain(String domain);

    /**
     * Used for content de-duplication tracking on hot upload paths.
     */
    Mono<BaseDocument> findByHash(String hash);
}