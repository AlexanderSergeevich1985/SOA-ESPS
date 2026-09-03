package com.soaesps.notifications.repository.reactive;

import com.soaesps.notifications.domain.reactive.TelegramContactRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Isolated CRUD repository for managing TELEGRAM bot notification endpoints.
 */
@Repository
public interface ReactiveTelegramContactRepository extends ReactiveCrudRepository<TelegramContactRow, Long> {

    /**
     * Finds all TELEGRAM configurations for a specific user.
     */
    @Query("SELECT id, user_id, contact_type, is_active, is_primary, created_at, telegram_chat_id, telegram_username " +
            "FROM user_contacts WHERE user_id = :userId AND contact_type = 'TELEGRAM'")
    Flux<TelegramContactRow> findTelegramByUserId(Long userId);
}