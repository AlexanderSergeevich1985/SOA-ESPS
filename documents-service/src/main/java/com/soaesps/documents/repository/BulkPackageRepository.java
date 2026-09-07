package com.soaesps.documents.repository;

import com.soaesps.documents.domain.BaseDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Dedicated repository layer managing flat non-hierarchical package bundles.
 */
@Repository
public interface BulkPackageRepository extends ReactiveMongoRepository<BaseDocument, String> {

    /**
     * Rapidly streams all equal documents bound to a shared clustering packet key.
     */
    Flux<BaseDocument> findAllByPackageId(String packageId);

    /**
     * Extracts only the structural virtual metadata header row for the package (docStatus = 99).
     */
    @Query("{ 'package_id': ?0, 'doc_status': 99 }")
    Mono<BaseDocument> findPackageHeader(String packageId);
}