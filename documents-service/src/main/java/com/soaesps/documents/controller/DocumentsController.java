package com.soaesps.documents.controller;

import com.soaesps.documents.domain.BaseDocument;
import com.soaesps.documents.service.DocumentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * Ultra-performance reactive REST controller managing secure document operations.
 * Operates on top of Spring WebFlux and Spring Security context layers using mTLS identity propagation.
 */
@RestController
@RequestMapping("/documents")
public class DocumentsController {

    private static final Logger log = LoggerFactory.getLogger(DocumentsController.class);

    private final DocumentsService documentsService;

    public DocumentsController(DocumentsService documentsService) {
        this.documentsService = documentsService;
    }

    /**
     * Securely resolves a specific document by its unique string identity.
     * Validates if the authenticated user holds valid access rights to view the payload.
     */
    @GetMapping("/{id}")
    public Mono<BaseDocument> getDocumentById(
            @AuthenticationPrincipal String authenticatedUserId, // Propagated via Gateway X-User-Id header filter
            @PathVariable String id) {

        long userAuthId = Long.parseLong(authenticatedUserId);
        log.debug("[Reactive-Controller] Read request captured for document ID: {} by user: {}", id, userAuthId);

        return documentsService.findDocumentById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Target document not found.")))
                .flatMap(doc -> validateDocumentAccess(doc, userAuthId));
    }

    /**
     * Registers and persists a fresh document layout structure.
     * Automatically stamps the owner and creator identities using the authenticated mTLS principal.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BaseDocument> createDocument(
            @AuthenticationPrincipal String authenticatedUserId,
            @RequestBody BaseDocument documentPayload) {

        long userAuthId = Long.parseLong(authenticatedUserId);
        log.info("[Reactive-Controller] Persisting new document profile for user ID: {}", userAuthId);

        // Enforce secure metadata parameters configuration before hitting MongoDB drivers
        documentPayload.setId(null); // Force database identity generation loop
        documentPayload.setCreateAuthId(userAuthId);
        documentPayload.setOwnerAuthId(userAuthId);

        return documentsService.save(documentPayload);
    }

    /**
     * Updates an existing document structure state.
     * Verifies data row ownership barriers dynamically prior to committing mutations to disk.
     */
    @PutMapping("/{id}")
    public Mono<BaseDocument> updateDocument(
            @AuthenticationPrincipal String authenticatedUserId,
            @PathVariable String id,
            @RequestBody BaseDocument updatePayload) {

        long userAuthId = Long.parseLong(authenticatedUserId);
        log.info("[Reactive-Controller] Updating document ID: {} requested by user: {}", id, userAuthId);

        return documentsService.findDocumentById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Document absent.")))
                .flatMap(existingDoc -> validateDocumentAccess(existingDoc, userAuthId))
                .flatMap(existingDoc -> {
                    // Map upmutable payload parameters safely onto the existing document entity context
                    existingDoc.setName(updatePayload.getName());
                    existingDoc.setEmail(updatePayload.getEmail());
                    existingDoc.setDocStatus(updatePayload.getDocStatus());
                    existingDoc.setProperties(updatePayload.getProperties());
                    existingDoc.setLastAuthId(userAuthId);

                    return documentsService.save(existingDoc);
                });
    }

    /**
     * Securely evicts/deletes a targeted document file metadata row from the MongoDB collection cluster.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteDocument(
            @AuthenticationPrincipal String authenticatedUserId,
            @PathVariable String id) {

        long userAuthId = Long.parseLong(authenticatedUserId);
        log.warn("[Reactive-Controller] Purging document record ID: {} triggered by user: {}", id, userAuthId);

        return documentsService.findDocumentById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Document absent.")))
                .flatMap(existingDoc -> validateDocumentAccess(existingDoc, userAuthId))
                .flatMap(validDoc -> documentsService.deleteById(validDoc.getId()));
    }

    // =========================================================================
    // PRIVATE INTERNAL REACTION DATA ISOLATION VALIDATORS
    // =========================================================================

    /**
     * Non-blocking verification step comparing document identity markers with the authenticated context.
     */
    private Mono<BaseDocument> validateDocumentAccess(BaseDocument document, long authenticatedUserId) {
        // Evaluate if the active requester matches either the creator or the legal current owner node
        if (document.getOwnerAuthId() == authenticatedUserId || document.getCreateAuthId() == authenticatedUserId) {
            return Mono.just(document); // Token matches, forward entity downstream cleanly
        }

        log.error("SECURITY BREACH: User ID {} attempted unauthorized access onto document ID {} owned by user {}",
                authenticatedUserId, document.getId(), document.getOwnerAuthId());

        // Return HTTP 403 Forbidden reactive signal preventing illegal malicious data inspection leaks
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: You do not own this document record parameters."));
    }
}