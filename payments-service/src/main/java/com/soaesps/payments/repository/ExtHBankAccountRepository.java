package com.soaesps.payments.repository;

import com.soaesps.payments.domain.transactions.ExtHBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExtHBankAccountRepository extends JpaRepository<ExtHBankAccount, Integer> {
}