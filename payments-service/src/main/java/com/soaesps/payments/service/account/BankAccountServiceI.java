package com.soaesps.payments.service.account;

import com.soaesps.payments.domain.transactions.BankAccount;

import javax.annotation.Nonnull;


public interface BankAccountServiceI {
    BankAccount registerAccount(@Nonnull final BankAccount account);

    boolean modifyAccount(@Nonnull final BankAccount accountNew);

    boolean deleteAccount(@Nonnull final Integer accountId);
}