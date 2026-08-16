package com.soaesps.payments.repository;

import com.soaesps.payments.DataModels.Transactions.BaseServerBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerBillsRepository extends JpaRepository<BaseServerBill, String> {
}