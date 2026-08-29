package com.soaesps.payments.domain.transactions;

import jakarta.persistence.*;

@Entity
@Table(name = "account_history")
public class AccountHistory {
    @Id
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "account_id")
    private BankAccount bankAccount;

    @Column(name = "archive_path", length = 256)
    private String archivePath;

    @Column(name = "password", length = 256)
    private String password;

    public AccountHistory() {}

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(final Long accountId) {
        this.accountId = accountId;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getArchivePath() {
        return archivePath;
    }

    public void setArchivePath(final String archivePath) {
        this.archivePath = archivePath;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}