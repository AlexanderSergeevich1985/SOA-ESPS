package com.soaesps.payments.domain.transactions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soaesps.core.DataModels.BaseEntity;
import org.hibernate.annotations.Type;

import javax.annotation.Nonnull;
import javax.persistence.*;

@Entity
@Table(name = "server_bills")
@Inheritance(strategy = InheritanceType.JOINED)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BankAccount extends BaseEntity {
    @Embedded
    private ServerBADesc serverBADesc;

    @Column(name = "indentation", length = 256, nullable = false)
    private String indentation;

    @Lob
    @Type(type="org.hibernate.type.BinaryType")
    @Column(name = "bill_signature", nullable = false)
    private byte[] billSignature;

    public BankAccount() {}

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