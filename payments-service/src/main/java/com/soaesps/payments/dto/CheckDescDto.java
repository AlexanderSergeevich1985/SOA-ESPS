package com.soaesps.payments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckDescDto {

    @NotNull(message = "Sign time is required")
    private Instant signTime;

    @NotBlank(message = "Payer ID cannot be empty")
    @Size(max = 500, message = "Payer ID must be under 500 characters")
    private String payerId;

    @NotBlank(message = "Payee ID cannot be empty")
    @Size(max = 500, message = "Payee ID must be under 500 characters")
    private String payeeId;

    @NotNull(message = "Transfer amount is required")
    @Positive(message = "Transfer amount must be greater than zero")
    private BigDecimal transferAmount;

    @NotNull(message = "Payer public key is required")
    private byte[] payerPublicKey;

    @NotNull(message = "Payee public key is required")
    private byte[] payeePublicKey;
}
