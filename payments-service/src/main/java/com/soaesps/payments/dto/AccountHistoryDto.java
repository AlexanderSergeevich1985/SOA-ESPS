package com.soaesps.payments.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountHistoryDto {

    // --- Primary Key shared with BankAccount via @MapsId ---
    private Long accountId;

    @Size(max = 256, message = "Archive path must be under 256 characters")
    private String archivePath;

    @Size(max = 256, message = "Password must be under 256 characters")
    private String password;
}