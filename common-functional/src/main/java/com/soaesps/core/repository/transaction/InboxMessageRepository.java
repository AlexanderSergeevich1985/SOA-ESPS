package com.soaesps.core.repository.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soaesps.core.DataModels.transaction.InboxMessage;

/**
 * Repository interface for managing idempotency logs within the inbox table boundary.
 */
@Repository
public interface InboxMessageRepository extends JpaRepository<InboxMessage, String> {
    // Standard JpaRepository brings built-in existsById() and save() features
}