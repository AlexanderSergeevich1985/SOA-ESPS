package com.soaesps.documents.service;

import com.soaesps.documents.domain.BaseDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentsService {
    Mono<BaseDocument> findDocumentById(final String id);
    Flux<BaseDocument> findAllByExample(final BaseDocument doc);
    Mono<BaseDocument> save(BaseDocument doc);
    Mono<Void> deleteById(final String id);
    Mono<Void> deleteByExample(final BaseDocument doc);
}