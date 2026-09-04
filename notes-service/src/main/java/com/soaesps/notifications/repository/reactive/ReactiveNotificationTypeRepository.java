package com.soaesps.notifications.repository.reactive;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Table("supported_notification_types")
record SupportedTypeRow(
        @Id Long id,
        @Column("type_name") String typeName,
        @Column("is_active") boolean active
) {}

@Repository
public interface ReactiveNotificationTypeRepository extends ReactiveCrudRepository<SupportedTypeRow, Long> {

    /**
     * Fetches all active notification type names from the database.
     */
    @Query("SELECT type_name FROM supported_notification_types WHERE is_active = true")
    Flux<String> findAllActiveTypes();
}