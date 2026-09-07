package com.soaesps.documentsservice.service;

import com.soaesps.documentsservice.DataModels.BaseDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DocumentsService {
    Mono<BaseDocument> findDocumentById(final String id);
    Flux<BaseDocument> findAllByExample(final BaseDocument doc);
    Mono<BaseDocument> save(BaseDocument doc);
    Mono<Void> deleteById(final String id);
    Mono<Void> deleteByExample(final BaseDocument doc);
}