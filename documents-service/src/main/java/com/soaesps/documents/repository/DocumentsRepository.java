package com.soaesps.documents.repository;

import com.soaesps.documents.domain.BaseDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentsRepository extends ReactiveMongoRepository<BaseDocument, Long> {
}