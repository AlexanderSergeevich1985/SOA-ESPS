package com.soaesps.documentsservice.repository;

import com.soaesps.documentsservice.DataModels.BaseDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentsRepository extends ReactiveMongoRepository<BaseDocument, Long> {
}