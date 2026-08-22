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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Embedded descriptor for a server-side bill containing cryptographic material.
 *
 * <p><b>SECURITY NOTE:</b> {@code privateKey} and {@code cipherKey} MUST NOT be persisted
 * in plaintext. The owning entity is responsible for encrypting these fields with a
 * KMS-managed master key before calling {@code save()} and decrypting them on load.
 * See {@code CryptoKeyEncryptionService} for the transparent encryption layer.
 */
@Embeddable
public class ServerBillDesc implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Column(name = "uuid", nullable = false, length = 16)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID uuid;

    @NotNull
    @Size(max = 500)
    @Column(name = "owner_id", nullable = false, length = 500)
    private String ownerId;

    @NotNull
    @Column(name = "owner_public_key", nullable = false, length = 4096)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] ownerPublicKey;

    @NotNull
    @Column(name = "public_key", nullable = false, length = 4096)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] publicKey;

    /**
     * MUST be encrypted with a KMS-managed master key before persistence.
     * The repository/service layer applies {@code CryptoKeyEncryptionService} transparently.
     */
    @NotNull
    @Column(name = "private_key_enc", nullable = false, length = 4096)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] privateKey;

    /**
     * Symmetric cipher key used to protect payload data.
     * Also MUST be encrypted with the master key before persistence.
     */
    @NotNull
    @Column(name = "cipher_key_enc", nullable = false, length = 2048)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] cipherKey;

    @NotNull
    @Column(name = "account_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal accountBalance;

    @Size(max = 500)
    @Column(name = "shared_secret", length = 500)
    private String sharedSecret;

    public ServerBillDesc() {}

    @Nonnull
    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(@Nonnull UUID uuid) {
        this.uuid = uuid;
    }

    @Nonnull
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(@Nonnull String ownerId) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerBillDesc that)) return false;
        return Objects.equals(uuid, that.uuid)
                && Objects.equals(ownerId, that.ownerId)
                && Arrays.equals(ownerPublicKey, that.ownerPublicKey)
                && Arrays.equals(publicKey, that.publicKey)
                && Arrays.equals(privateKey, that.privateKey)
                && Arrays.equals(cipherKey, that.cipherKey)
                && Objects.equals(accountBalance, that.accountBalance)
                && Objects.equals(sharedSecret, that.sharedSecret);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(uuid, ownerId, accountBalance, sharedSecret);
        result = 31 * result + Arrays.hashCode(ownerPublicKey);
        result = 31 * result + Arrays.hashCode(publicKey);
        result = 31 * result + Arrays.hashCode(privateKey);
        result = 31 * result + Arrays.hashCode(cipherKey);
        return result;
    }
}