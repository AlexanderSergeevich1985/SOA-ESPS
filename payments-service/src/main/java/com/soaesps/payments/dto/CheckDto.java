package com.soaesps.payments.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckDto {

    private Long id;

    @NotNull(message = "Check description is required")
    private CheckDescDto checkDesc;

    @NotNull(message = "Check signature is required")
    private byte[] checkSignature;
}