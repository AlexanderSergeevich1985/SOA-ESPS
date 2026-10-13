package com.soaesps.payments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.soaesps.payments.DataModels.Transactions.TransactionDesc;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;

/**
 * Data Transfer Object matching the exact structure of the TransactionDesc embeddable entity.
 */
public record TransactionDescDto(
        @NotNull(message = "Signature timestamp is required")
        @JsonProperty("signTime")
        ZonedDateTime signTime, // Automatically maps from ISO-8601 strings and handles timezones cleanly

        @NotBlank(message = "Payer ID cannot be blank")
        @JsonProperty("payerId")
        String payerId,

        @NotBlank(message = "Payee ID cannot be blank")
        @JsonProperty("payeeId")
        String payeeId,

        @NotNull(message = "Transfer amount cannot be null")
        @Positive(message = "Transfer amount must be greater than zero")
        @JsonProperty("transferAmount")
        BigDecimal transferAmount,

        @NotBlank(message = "Payer public key is required")
        @JsonProperty("payerPublicKey")
        String payerPublicKey // Transmitted as Base64 encoded string
) { }