package com.soaesps.payments.domain.transactions;

import jakarta.persistence.*;

@Entity
@Table(name = "bank_account")
public class BankAccount extends BaseBankAccount {
    @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private AccountHistory history;

    public AccountHistory getHistory() {
        return history;
    }

    public void setHistory(final AccountHistory history) {
        this.history = history;
    }
}