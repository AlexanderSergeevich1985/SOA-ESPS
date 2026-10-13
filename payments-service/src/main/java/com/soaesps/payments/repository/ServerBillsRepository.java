package com.soaesps.payments.repository;

import com.soaesps.payments.DataModels.Transactions.BaseServerBill;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServerBillsRepository extends JpaRepository<BaseServerBill, Long> {
    /**
     * Acquires a pessimistic write lock (SELECT ... FOR UPDATE) on the bill record.
     * USED FOR WRITE OPERATIONS (Transfer, Cash Check).
     * This prevents Race Conditions and lost updates by forcing concurrent threads to wait.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<BaseServerBill> findAndLockByServerBillDesc_Uuid(UUID uuid);

    /**
     * USED FOR READ OPERATIONS (Refresh, Balance View).
     * Standard non-blocking SELECT query for maximum throughput.
     */
    Optional<BaseServerBill> findByServerBillDesc_Uuid(UUID uuid);
}