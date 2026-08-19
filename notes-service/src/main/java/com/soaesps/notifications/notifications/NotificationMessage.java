package com.soaesps.notifications.notifications;

public record NotificationMessage(
        String subject,
        String body,
        NotificationType type   // OTP, TRANSACTION_ALERT, PROMO, ...
) {}

