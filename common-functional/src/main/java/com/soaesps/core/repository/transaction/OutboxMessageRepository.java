package com.soaesps.core.repository.transaction;

import com.soaesps.core.DataModels.transaction.OutboxMessage;
import com.soaesps.core.dto.MessageStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing outbox messages in the database.
 */
@Repository
public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, String> {

    /**
     * Retrieves a limited batch of messages with the specified status, ordered by creation time.
     * Commonly used by background schedulers to process pending events in chunks.
     *
     * @param status   the status of the messages to find
     * @param pageable the pagination configuration to limit the result size
     * @return a list of matching outbox messages
     */
    List<OutboxMessage> findByStatusOrderByCreatedAtAsc(MessageStatus status, Pageable pageable);

    /**
     * Atomically updates the processing status of a specific message.
     *
     * @param id     the unique identifier of the message
     * @param status the new status to apply
     */
    @Modifying
    @Query("UPDATE OutboxMessage o SET o.status = :status WHERE o.id = :id")
    void updateStatus(@Param("id") String id, @Param("status") MessageStatus status);

    /**
     * Atomically increments the delivery retry counter for a specific message.
     *
     * @param id the unique identifier of the message
     */
    @Modifying
    @Query("UPDATE OutboxMessage o SET o.retryCount = o.retryCount + 1 WHERE o.id = :id")
    void incrementRetryCount(@Param("id") String id);

    /**
     * Counts the number of messages with the specified status.
     * Useful for health checks, system metrics, and alerting systems.
     *
     * @param status the status to count
     * @return the total number of matching messages
     */
    long countByStatus(MessageStatus status);

    /**
     * Retrieves all messages with the specified status, ordered by creation time.
     * Typically used for manual dead-letter queue (DLQ) inspection or reprocessing.
     *
     * @param status the status of the messages to find
     * @return a ordered list of matching outbox messages
     */
    List<OutboxMessage> findByStatusOrderByCreatedAtAsc(MessageStatus status);
}