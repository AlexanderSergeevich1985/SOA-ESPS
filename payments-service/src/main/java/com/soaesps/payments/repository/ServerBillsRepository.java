package com.soaesps.payments.repository;

import com.soaesps.payments.DataModels.Transactions.BaseServerBill;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerBillsRepository extends CrudRepository<BaseServerBill, String> {
    BaseServerBill findOne(String name);
}