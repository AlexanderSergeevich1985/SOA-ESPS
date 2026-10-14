package com.soaesps.payments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDto {

    // --- Inherited from BaseEntity ---

    private Long id;

    private ZonedDateTime creationTime;

    private ZonedDateTime modificationTime;

    // --- Inherited from BaseBankAccount ---

    @NotNull(message = "Server BA description is required")
    private ServerBADescDto serverBADesc;

    @NotBlank(message = "Indentation cannot be empty")
    @Size(max = 256, message = "Indentation must be under 256 characters")
    private String indentation;

    @NotNull(message = "Bill signature is required")
    private byte[] billSignature;

    // --- BankAccount specific properties ---

    private AccountHistoryDto history; // Assuming BankAccount defines a relationship with AccountHistory
}