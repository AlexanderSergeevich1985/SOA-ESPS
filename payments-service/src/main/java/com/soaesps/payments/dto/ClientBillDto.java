package com.soaesps.payments.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

/**
 * Data Transfer Object representing the top-level client bill response.
 */
public record ClientBillDto(
        @NotNull
        @JsonProperty("id")
        Long id,

        @JsonProperty("creationTime")
        ZonedDateTime creationTime,

        @NotNull
        @JsonProperty("billDescription")
        ClientBillDescDto clientBillDesc,

        @NotBlank
        @JsonProperty("signature")
        String billSignature
) { }