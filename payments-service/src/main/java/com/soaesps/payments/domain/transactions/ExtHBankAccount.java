package com.soaesps.payments.domain.transactions;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "account_history")
@PrimaryKeyJoinColumn(name = "account_id")
public class ExtHBankAccount extends BankAccount {
    @Column(name = "archive_path", length = 256)
    private String archivePath;

    @Column(name = "password", length = 256)
    private String password;

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