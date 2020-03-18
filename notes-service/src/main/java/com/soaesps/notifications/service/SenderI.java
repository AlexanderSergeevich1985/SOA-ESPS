package com.soaesps.notifications.service;

import javax.mail.internet.MimeMessage;

public interface SenderI {
    void send(final MimeMessage message);
}