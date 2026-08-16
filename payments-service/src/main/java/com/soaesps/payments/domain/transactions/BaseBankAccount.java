package com.soaesps.payments.domain.transactions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soaesps.core.DataModels.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;

/**
 * Base entity for bank account models.
 * Uses @MappedSuperclass to inherit common fields without creating a separate table.
 */
@MappedSuperclass
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BaseBankAccount extends BaseEntity {

    @Embedded
    private ServerBADesc serverBADesc;

    @Column(name = "indentation", length = 256, nullable = false)
    private String indentation;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "bill_signature", nullable = false)
    private byte[] billSignature;

    public BaseBankAccount() {}

    @Nonnull
    public ServerBADesc getServerBADesc() {
        return serverBADesc;
    }

    public void setServerBADesc(@Nonnull ServerBADesc serverBADesc) {
        this.serverBADesc = serverBADesc;
    }

    @Nonnull
    public String getIndentation() {
        return indentation;
    }

    public void setIndentation(@Nonnull String indentation) {
        this.indentation = indentation;
    }

    @Nonnull
    public byte[] getBillSignature() {
        return billSignature;
    }

    public void setBillSignature(@Nonnull byte[] billSignature) {
        this.billSignature = billSignature;
    }
}