package com.soaesps.payments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Clean Data Transfer Object for ClientBillDesc.
 * Excludes internal entity states, database mappings, and deprecated security fields.
 */
public record ClientBillDescDto(
        @NotNull(message = "Account UUID cannot be null")
        @JsonProperty("uuid")
        UUID uuid,

        @NotBlank(message = "Issuer ID cannot be blank")
        @JsonProperty("issuerId")
        String issuerId,

        @NotNull(message = "Account balance cannot be null")
        @PositiveOrZero(message = "Account balance cannot be negative")
        @JsonProperty("accountBalance")
        BigDecimal accountBalance,

        @NotBlank(message = "Server public key is required for client verification")
        @JsonProperty("serverPublicKey")
        String serverPublicKey // Base64 encoded representation for clean JSON transit
) {}