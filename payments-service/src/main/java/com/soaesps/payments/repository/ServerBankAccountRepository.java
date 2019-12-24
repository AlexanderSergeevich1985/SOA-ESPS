package com.soaesps.payments.repository;

import com.soaesps.payments.domain.transactions.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerBankAccountRepository extends JpaRepository<BankAccount, Integer> {
    //@Query("SELECT DISTINCT ")
    //BaseBankAccount findDistinctFirstByServerBillDesc_OwnerId(String name);
}