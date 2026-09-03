package com.soaesps.notifications.repository.reactive;

import com.soaesps.notifications.domain.reactive.EmailContactRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Isolated CRUD repository for managing EMAIL notification endpoints.
 */
@Repository
public interface ReactiveEmailContactRepository extends ReactiveCrudRepository<EmailContactRow, Long> {

    /**
     * Finds all EMAIL configurations for a specific user.
     */
    @Query("SELECT id, user_id, contact_type, is_active, is_primary, created_at, email_address " +
            "FROM user_contacts WHERE user_id = :userId AND contact_type = 'EMAIL'")
    Flux<EmailContactRow> findEmailByUserId(Long userId);
}