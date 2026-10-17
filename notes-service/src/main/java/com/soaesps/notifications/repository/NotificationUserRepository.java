package com.soaesps.notifications.repository;

import com.soaesps.notifications.domain.NotificationUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository managing the root NotificationUser aggregate.
 */
@Repository
public interface NotificationUserRepository extends JpaRepository<NotificationUser, UUID> {
    // Standard CRUD operations are fully inherited
}