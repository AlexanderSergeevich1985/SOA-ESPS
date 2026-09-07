package com.soaesps.documents.repository;

import com.soaesps.documents.domain.BaseDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Manages parent-child tree dependencies using fast indexed references.
 */
@Repository
public interface HierarchicalDocumentRepository extends ReactiveMongoRepository<BaseDocument, String> {

    /**
     * Fetches immediate children nodes linked directly to the upstream parent element.
     */
    Flux<BaseDocument> findAllByParentId(String parentId);
}
