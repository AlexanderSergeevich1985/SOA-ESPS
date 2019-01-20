package com.soaesps.documentsservice.service;

import com.soaesps.documentsservice.DataModels.BaseDocument;
import com.soaesps.documentsservice.repository.DocumentsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service("DocumentsService")
public class DocumentsServiceImpl implements DocumentsService {
    static private final Logger logger;

    static {
        logger = Logger.getLogger(DocumentsServiceImpl.class.getName());
        logger.setLevel(Level.INFO);
    }

    @Autowired
    private DocumentsRepository repository;

    public DocumentsServiceImpl() {}

    @Override
    public Mono<BaseDocument> findDocumentById(final Long id) {
        return repository.findById(id);
    }

    @Override
    public Flux<BaseDocument> findAllByExample(final BaseDocument doc) {
        return repository.findAll(Example.of(doc));
    }

    @Override
    public Mono<BaseDocument> save(final BaseDocument doc) {
        return repository.save(doc);
    }

    public Flux<BaseDocument> update(final BaseDocument doc) {
        return this.repository.findAll(Example.of(doc))
                .map(p -> new BaseDocument(p))
                .flatMap(this.repository::save);
    }

    @Override
    public Mono<Void> deleteById(final Long id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<Void> deleteByExample(final BaseDocument doc) {
        return this.repository.delete(doc);
    }
}