package com.soaesps.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "Old password cannot be empty")
        String oldPassword,

        @NotBlank(message = "New password cannot be empty")
        String newPassword
) {}