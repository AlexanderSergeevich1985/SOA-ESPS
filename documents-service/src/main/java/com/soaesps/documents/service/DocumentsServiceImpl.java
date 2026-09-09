package com.soaesps.documents.service;

import com.soaesps.documents.domain.BaseDocument;
import com.soaesps.documents.repository.BulkPackageRepository;
import com.soaesps.documents.repository.HierarchicalDocumentRepository;
import com.soaesps.documents.repository.StandaloneDocumentRepository;
import com.soaesps.documents.validation.DynamicDocumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Enterprise reactive implementation of the DocumentsService contract layer.
 * Coordinates multi-faceted business paths by routing requests to specialized fine-grained repositories
 * and enforcing strict data-driven validation policies.
 */
@Service("DocumentsService")
public class DocumentsServiceImpl implements DocumentsService {

    private static final Logger log = LoggerFactory.getLogger(DocumentsServiceImpl.class);

    private final StandaloneDocumentRepository standaloneRepository;
    private final BulkPackageRepository bulkPackageRepository;
    private final HierarchicalDocumentRepository hierarchicalDocumentRepository;

    private final DynamicDocumentValidator dynamicValidator;

    public DocumentsServiceImpl(StandaloneDocumentRepository standaloneRepository,
                                BulkPackageRepository bulkPackageRepository,
                                HierarchicalDocumentRepository hierarchicalDocumentRepository,
                                DynamicDocumentValidator dynamicValidator) {
        this.standaloneRepository = standaloneRepository;
        this.bulkPackageRepository = bulkPackageRepository;
        this.hierarchicalDocumentRepository = hierarchicalDocumentRepository;
        this.dynamicValidator = dynamicValidator;
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
        log.info("Persisting raw unvalidated document state into MongoDB bounds for: {}", doc.getName());
        return standaloneRepository.save(doc);
    }

    /**
     * Enhanced non-blocking save operation wrapped with dynamic schema validation.
     * Use this method on hot creation and synchronization execution paths.
     */
    public Mono<BaseDocument> save(final BaseDocument doc, final String documentType) {
        log.info("Executing dynamic validation step prior to saving document for type: {}", documentType);
        return dynamicValidator.validate(documentType, doc)
                .then(standaloneRepository.save(doc));
    }

    /**
     * Non-blockingly intercepts, duplicates copies, and updates records via fluent pipeline flows.
     * FIX: Injected dynamic schema validation check to evaluate every mutated clone before hitting disk.
     * Assumes document type identifier is extracted from the entity metadata name or attributes context.
     */
    public Flux<BaseDocument> update(final BaseDocument doc) {
        log.info("Initiating batch mutation update sequence for document nodes matching constraints");
        return standaloneRepository.findAll(Example.of(doc))
                .map(BaseDocument::new) // Safely clone existing states via the copy constructor structure
                .flatMap(clonedDoc -> {
                    // Extracting the dynamic document type signature from the model parameters field
                    String inferredType = clonedDoc.getName();
                    log.debug("Validating cloned document mutation node for inferred type: {}", inferredType);

                    return dynamicValidator.validate(inferredType, clonedDoc)
                            .then(standaloneRepository.save(clonedDoc));
                });
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