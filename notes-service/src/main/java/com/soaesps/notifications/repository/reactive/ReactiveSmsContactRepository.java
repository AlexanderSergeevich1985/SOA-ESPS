package com.soaesps.notifications.repository.reactive;

import com.soaesps.notifications.domain.reactive.SmsContactRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Isolated CRUD repository for managing SMS notification endpoints.
 */
@Repository
public interface ReactiveSmsContactRepository extends ReactiveCrudRepository<SmsContactRow, Long> {

    /**
     * Finds all active SMS configurations for a specific user.
     */
    @Query("SELECT id, user_id, contact_type, is_active, is_primary, created_at, phone_number " +
            "FROM user_contacts WHERE user_id = :userId AND contact_type = 'SMS'")
    Flux<SmsContactRow> findSmsByUserId(Long userId);
}