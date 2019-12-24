package com.soaesps.payments.domain.transactions;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "account_history")
public class AccountHistory {
    @Id
    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "archive_path", length = 256)
    private String archivePath;

    @Column(name = "password", length = 256)
    private String password;

    public AccountHistory() {}

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(final Integer accountId) {
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