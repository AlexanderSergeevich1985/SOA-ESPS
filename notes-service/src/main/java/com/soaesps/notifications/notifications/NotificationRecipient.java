package com.soaesps.notifications.notifications;

import java.util.List;
import java.util.Set;

public record NotificationRecipient(
        Long userId,
        String phone,
        String email,
        String telegramChatId,
        List<String> pushTokens,
        Set<NotificationType> activeChannels
) {
    public enum NotificationType {
        EMAIL, SMS, TELEGRAM, PUSH
    }
}