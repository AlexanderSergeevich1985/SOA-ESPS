package com.soaesps.notifications.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Represents a Telegram bot communication channel endpoint.
 */
@Entity
@DiscriminatorValue("TELEGRAM")
public class TelegramContact extends UserContact {

    @Column(name = "telegram_chat_id") //, unique = true
    @NotBlank(message = "Telegram chat ID cannot be blank")
    private String telegramChatId;

    @Column(name = "telegram_username")
    @NotBlank(message = "Telegram username cannot be blank")
    @Pattern(
            regexp = "^@[a-zA-Z0-9_]{5,32}$",
            message = "Telegram username must start with @ and be between 5 and 32 characters (letters, numbers, underscores)"
    )
    private String username;

    public String getTelegramChatId() {
        return telegramChatId;
    }

    public void setTelegramChatId(String telegramChatId) {
        this.telegramChatId = telegramChatId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}