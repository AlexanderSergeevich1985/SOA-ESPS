package com.soaesps.payments.domain.transactions;

import javax.persistence.*;

@Entity
@Table(name = "server_bills")
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