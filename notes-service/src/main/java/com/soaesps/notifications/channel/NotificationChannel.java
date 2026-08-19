package com.soaesps.notifications.channel;


import com.soaesps.notifications.notifications.NotificationMessage;
import com.soaesps.notifications.notifications.NotificationRecipient;

/**
 * Abstraction for a single delivery channel (SMS, Telegram, Email, Push).
 * Each implementation is a Spring bean and participates in the routing logic.
 */
public interface NotificationChannel {

    /** Unique channel id: "sms", "telegram", "email", "push". */
    String id();

    /** Whether this channel can deliver to the given recipient descriptor. */
    boolean supports(NotificationRecipient recipient);

    /** Priority: lower value = tried first. */
    int priority();

    /** Deliver the message. Returns true on success. */
    boolean send(NotificationMessage message, NotificationRecipient recipient);
}