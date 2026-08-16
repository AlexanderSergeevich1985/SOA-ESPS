package com.soaesps.notifications.service;

import jakarta.mail.internet.MimeMessage;

public interface SenderI {
    void send(final MimeMessage message);
}