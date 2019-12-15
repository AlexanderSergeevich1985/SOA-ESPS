package com.soaesps.payments.domain.transactions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.soaesps.core.DataModels.BaseEntity;

import javax.annotation.Nonnull;
import javax.persistence.*;

@Entity
@Table(name = "server_bills")//@Table(name = "soa_esps.server_bills") //name = "soa_esps.
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BankAccount extends BaseEntity {
    /*@Id
    @GenericGenerator(name="kaugen" , strategy="increment")
    @GeneratedValue(generator="kaugen")
    @Column(name = "id", nullable = false)
    private Integer id;*/

    @Embedded
    private ServerBADesc serverBADesc;

    @Column(name = "indentation", length = 256, nullable = false)
    private String indentation;

    //@Type(type="org.hibernate.type.BinaryType")
    //@Column(name = "bill_signature", nullable = false)
    @Transient
    private byte[] billSignature;

    public BankAccount() {}

    /*public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }*/

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