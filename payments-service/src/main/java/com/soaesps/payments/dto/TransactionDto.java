package com.soaesps.payments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object representing the top-level transaction data structure.
 * Designed as a safe, immutable Java Record with an embedded Fluent Builder.
 */
public record TransactionDto(
        @JsonProperty("transactionId")
        Long transactionId,

        @JsonProperty("knownMsg")
        String knownMsg,

        @Valid
        @NotNull(message = "Transaction descriptor details are required")
        @JsonProperty("transactionDescriptor")
        TransactionDescDto transactionDescriptor,

        @NotBlank(message = "Cryptographic transaction signature is required")
        @JsonProperty("transactionSignature")
        String transactionSignature // Base64 encoded string representing the binary signature
) { }