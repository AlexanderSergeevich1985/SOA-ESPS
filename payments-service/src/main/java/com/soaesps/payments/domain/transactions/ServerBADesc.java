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
package com.soaesps.payments.domain.transactions;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Embeddable descriptor for bank account cryptographic and ownership details.
 */
@Embeddable
public class ServerBADesc implements Serializable {

    @Column(name = "uuid", columnDefinition = "BINARY(32)", nullable = false)
    private UUID uuid;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "owner_public_key", nullable = false)
    private byte[] ownerPublicKey;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "public_key", nullable = false)
    private byte[] publicKey;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "private_key", nullable = false)
    private byte[] privateKey;

    @Lob
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "cipher_key", nullable = false)
    private byte[] cipherKey;

    @Column(name = "account_balance", nullable = false)
    private BigDecimal accountBalance;

    @Column(name = "shared_secret")
    @Size(max = 500)
    private String sharedSecret;

    public ServerBADesc() {}

    @Nonnull
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(@Nonnull UUID uuid) {
        this.uuid = uuid;
    }

    @Nonnull
    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(@Nonnull Long ownerId) {
        this.ownerId = ownerId;
    }

    @Nonnull
    public byte[] getOwnerPublicKey() {
        return ownerPublicKey;
    }

    public void setOwnerPublicKey(@Nonnull byte[] ownerPublicKey) {
        this.ownerPublicKey = ownerPublicKey;
    }

    @Nonnull
    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(@Nonnull byte[] publicKey) {
        this.publicKey = publicKey;
    }

    @Nonnull
    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(@Nonnull byte[] privateKey) {
        this.privateKey = privateKey;
    }

    @Nonnull
    public byte[] getCipherKey() {
        return cipherKey;
    }

    public void setCipherKey(@Nonnull byte[] cipherKey) {
        this.cipherKey = cipherKey;
    }

    @Nonnull
    public BigDecimal getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(@Nonnull BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    @Nullable
    public String getSharedSecret() {
        return sharedSecret;
    }

    public void setSharedSecret(@Nullable String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }
}