package com.soaesps.core.repository.transaction;

import com.soaesps.core.DataModels.transaction.FailedOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FailedOutboxEventRepository extends JpaRepository<FailedOutboxEvent, Long> {
}