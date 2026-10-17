package com.soaesps.notifications.repository.reactive;

import com.soaesps.notifications.domain.reactive.EmailContactRow;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

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
    Flux<EmailContactRow> findEmailByUserId(UUID userId);

    @Query("WITH inserted_user AS (" +
            "  INSERT INTO notification_users (user_id) " +
            "  VALUES ( :#{#contact.userId} ) " +
            "  ON CONFLICT (user_id) DO NOTHING " +
            "  RETURNING user_id" +
            ") " +
            "INSERT INTO user_contacts (" +
            "  user_id, contact_type, is_active, is_primary, created_at, " +
            "  email_address, " +
            "  telegram_chat_id, telegram_username, device_id, device_type, push_token, phone_number" +
            ") " +
            "SELECT " +
            "  :#{#contact.userId}, :#{#contact.contactType}, :#{#contact.active}, :#{#contact.primary}, :#{#contact.createdAt}, " +
            "  :#{#contact.emailAddress}, " +
            "  NULL, NULL, NULL, NULL, NULL, NULL " +
            "FROM ( " +
            "  SELECT 1 FROM (SELECT 1) d " +
            "  LEFT JOIN inserted_user ON TRUE " +
            "  LIMIT 1" +
            ") sub " +
            "RETURNING id, user_id, contact_type, is_active, is_primary, created_at, email_address")
    Mono<EmailContactRow> saveWithUser(@Param("contact") EmailContactRow contact);
}