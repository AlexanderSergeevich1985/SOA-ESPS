package com.soaesps.notifications.repository;

import com.soaesps.notifications.domain.NotificationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository managing the root NotificationUser aggregate.
 */
@Repository
public interface NotificationUserRepository extends JpaRepository<NotificationUser, Long> {
    // Standard CRUD operations are fully inherited
}