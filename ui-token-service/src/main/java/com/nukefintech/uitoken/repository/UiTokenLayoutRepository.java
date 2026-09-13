package com.nukefintech.uitoken.repository;

import com.nukefintech.uitoken.domain.UiTokenLayout;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UiTokenLayoutRepository extends ReactiveMongoRepository<UiTokenLayout, String> {

    /**
     * Resolves token configurations matrix block using atomic domain specifications mapping.
     */
    Mono<UiTokenLayout> findByDocumentTypeAndViewModeAndDensity(String documentType, String viewMode, String density);
}