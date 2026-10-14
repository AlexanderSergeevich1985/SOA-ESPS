package com.soaesps.payments.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerBADescDto {

    @NotNull(message = "UUID cannot be null")
    private UUID uuid;

    @NotNull(message = "Owner ID cannot be null")
    private Long ownerId;

    @NotNull(message = "Owner public key cannot be null")
    private byte[] ownerPublicKey;

    @NotNull(message = "Public key cannot be null")
    private byte[] publicKey;

    @NotNull(message = "Private key cannot be null")
    private byte[] privateKey;

    @NotNull(message = "Cipher key cannot be null")
    private byte[] cipherKey;

    @NotNull(message = "Account balance cannot be null")
    private BigDecimal accountBalance;

    @Size(max = 500, message = "Shared secret must be under 500 characters")
    private String sharedSecret;
}