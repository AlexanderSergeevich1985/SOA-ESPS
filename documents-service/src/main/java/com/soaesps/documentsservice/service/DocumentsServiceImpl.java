package com.soaesps.documentsservice.service;

import com.soaesps.documentsservice.DataModels.BaseDocument;
import com.soaesps.documentsservice.repository.BulkPackageRepository;
import com.soaesps.documentsservice.repository.HierarchicalDocumentRepository;
import com.soaesps.documentsservice.repository.StandaloneDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Enterprise reactive implementation of the DocumentsService contract layer.
 * Coordinates multi-faceted business paths by routing requests to specialized fine-grained repositories.
 */
@Service("DocumentsService")
public class DocumentsServiceImpl implements DocumentsService {

    private static final Logger log = LoggerFactory.getLogger(DocumentsServiceImpl.class);

    // Injected distinct reactive repositories targeting the same physical MongoDB collection bounds
    private final StandaloneDocumentRepository standaloneRepository;
    private final BulkPackageRepository bulkPackageRepository;
    private final HierarchicalDocumentRepository hierarchicalDocumentRepository;

    public DocumentsServiceImpl(StandaloneDocumentRepository standaloneRepository,
                                BulkPackageRepository bulkPackageRepository,
                                HierarchicalDocumentRepository hierarchicalDocumentRepository) {
        this.standaloneRepository = standaloneRepository;
        this.bulkPackageRepository = bulkPackageRepository;
        this.hierarchicalDocumentRepository = hierarchicalDocumentRepository;
    }

    @Override
    public Mono<BaseDocument> findDocumentById(final String id) {
        log.debug("Executing non-blocking lookup for document ID: {}", id);
        return standaloneRepository.findById(id);
    }

    @Override
    public Flux<BaseDocument> findAllByExample(final BaseDocument doc) {
        log.debug("Streaming documents matching reactive Example criteria query layout");
        return standaloneRepository.findAll(Example.of(doc));
    }

    @Override
    public Mono<BaseDocument> save(final BaseDocument doc) {
        log.info("Persisting reactive document state into MongoDB collection bounds for: {}", doc.getName());
        return standaloneRepository.save(doc);
    }

    /**
     * Non-blockingly intercepts, duplicates copies, and updates records via fluent pipeline flows.
     */
    public Flux<BaseDocument> update(final BaseDocument doc) {
        log.info("Initiating batch mutation update sequence for document nodes matching constraints");
        return standaloneRepository.findAll(Example.of(doc))
                .map(BaseDocument::new) // Safely clone existing states via the copy constructor structure
                .flatMap(standaloneRepository::save); // Parallel asynchronous write stream fan-out loops
    }

    @Override
    public Mono<Void> deleteById(final String id) {
        log.warn("Triggering physical transactional row removal for document primary key ID: {}", id);
        return standaloneRepository.deleteById(id);
    }

    @Override
    public Mono<Void> deleteByExample(final BaseDocument doc) {
        log.warn("Initiating bulk criteria-driven document eviction mappings matching example");
        return standaloneRepository.delete(doc);
    }

    // =========================================================================
    // SPECIALIZED FACET PIPELINE EXTENSIONS
    // =========================================================================

    /**
     * Facet extension routing task execution directly to the Package domain repository.
     */
    public Flux<BaseDocument> fetchBulkPackageContents(String packageId) {
        log.debug("Streaming flat package cluster matching shard token index: {}", packageId);
        return bulkPackageRepository.findAllByPackageId(packageId);
    }

    /**
     * Facet extension routing task execution directly to the Tree Hierarchy repository.
     */
    public Flux<BaseDocument> fetchImmediateDependents(String parentId) {
        log.debug("Streaming immediate descendant child nodes from parent coordinate: {}", parentId);
        return hierarchicalDocumentRepository.findAllByParentId(parentId);
    }
}