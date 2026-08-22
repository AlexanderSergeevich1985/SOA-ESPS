package com.soaesps.payments.domain.transactions;

import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Embeddable descriptor for bank account cryptographic and ownership details.
 */
@Embeddable
public class ServerBADesc implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    @Column(name = "uuid", nullable = false, length = 16)
    @JdbcTypeCode(SqlTypes.BINARY)
    private UUID uuid;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "owner_public_key", nullable = false, length = 4096)
    private byte[] ownerPublicKey;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "public_key", nullable = false, length = 4096)
    private byte[] publicKey;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "private_key", nullable = false, length = 4096)
    private byte[] privateKey;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "cipher_key", nullable = false, length = 512)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerBADesc that)) return false;
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