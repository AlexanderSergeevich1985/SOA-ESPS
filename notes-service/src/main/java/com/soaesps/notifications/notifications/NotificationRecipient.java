package com.soaesps.notifications.notifications;

public record NotificationRecipient(
        Long userId,
        String phone,           // +79991234567
        String email,
        String telegramChatId,  // optional, если пользователь привязал Telegram
        String pushToken        // optional
) {}
