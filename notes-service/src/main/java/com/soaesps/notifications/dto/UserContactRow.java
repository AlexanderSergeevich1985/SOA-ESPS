package com.soaesps.notifications.dto;

public record UserContactRow(
        String contactType,
        String emailAddress,
        String telegramChatId,
        String pushToken,
        String phoneNumber
) {}