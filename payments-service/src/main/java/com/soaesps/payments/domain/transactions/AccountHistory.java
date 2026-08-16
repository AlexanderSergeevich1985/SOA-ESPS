package com.soaesps.payments.domain.transactions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "account_history")
public class AccountHistory {
    @Id
    @Column(name = "account_id", nullable = false)
    private Long accountId;

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