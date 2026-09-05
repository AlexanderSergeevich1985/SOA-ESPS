package com.soaesps.notifications.repository.reactive;

import com.soaesps.notifications.dto.UserContactRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ReactiveContactRepository extends ReactiveCrudRepository<UserContactRow, Long> {

    @Query("SELECT contact_type, email_address, telegram_chat_id, push_token, phone_number " +
            "FROM user_contacts WHERE user_id = :userId")
    Flux<UserContactRow> findByUserId(Long userId);
}
