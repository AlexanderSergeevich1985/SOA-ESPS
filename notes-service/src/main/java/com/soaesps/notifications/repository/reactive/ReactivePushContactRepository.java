package com.soaesps.notifications.repository.reactive;

import com.soaesps.notifications.domain.reactive.PushContactRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Isolated CRUD repository for managing mobile/web PUSH notification tokens.
 */
@Repository
public interface ReactivePushContactRepository extends ReactiveCrudRepository<PushContactRow, Long> {

    /**
     * Finds all registered PUSH devices for a specific user.
     */
    @Query("SELECT id, user_id, contact_type, is_active, is_primary, created_at, push_token, device_id, device_type " +
            "FROM user_contacts WHERE user_id = :userId AND contact_type = 'PUSH'")
    Flux<PushContactRow> findPushByUserId(Long userId);
}