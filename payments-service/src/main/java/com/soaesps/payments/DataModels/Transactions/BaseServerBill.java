/**The MIT License (MIT)
 Copyright (c) 2018 by AleksanderSergeevich
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package com.soaesps.payments.DataModels.Transactions;

import com.soaesps.core.DataModels.BaseEntity;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serial;

/**
 * Persistent entity representing a server-side bill with cryptographic material.
 *
 * <p>Inherits ID, versioning and audit fields from {@link BaseEntity}.
 * The embedded {@link ServerBillDesc} holds keys and balance; this entity
 * adds the bill signature and a human-readable identifier.
 *
 * <p><b>SECURITY:</b> {@code billSignature} is a public cryptographic structure
 * (e.g. Ed25519 signature) and does NOT require encryption at rest.
 * Private keys inside {@link ServerBillDesc} MUST be encrypted before persistence
 * — see {@code CryptoKeyEncryptionService}.
 */
@Entity
@Table(name = "SERVER_BILLS", indexes = {
        // Index on ownerId inside the embedded ServerBillDesc
        @Index(name = "idx_server_bills_owner_id", columnList = "owner_id"),
        @Index(name = "idx_server_bills_uuid", columnList = "uuid", unique = true)
})
public class BaseServerBill extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @Embedded
    private ServerBillDesc serverBillDesc;

    @NotNull
    @Size(max = 256)
    @Column(name = "indentation", length = 256, nullable = false)
    private String indentation;

    @NotNull
    @Column(name = "bill_signature", nullable = false, length = 1024)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Basic(fetch = FetchType.LAZY)
    private byte[] billSignature;

    public BaseServerBill() {}

    @Nonnull
    public ServerBillDesc getServerBillDesc() {
        return serverBillDesc;
    }

    public void setServerBillDesc(@Nonnull ServerBillDesc serverBillDesc) {
        this.serverBillDesc = serverBillDesc;
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