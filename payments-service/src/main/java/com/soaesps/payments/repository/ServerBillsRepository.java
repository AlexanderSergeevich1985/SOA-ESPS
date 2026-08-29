package com.soaesps.payments.repository;

import com.soaesps.payments.DataModels.Transactions.BaseServerBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServerBillsRepository extends JpaRepository<BaseServerBill, Long> {
    Optional<BaseServerBill> findByServerBillDesc_Uuid(UUID uuid);
}