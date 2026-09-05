package com.soaesps.notifications.channel;

import com.soaesps.notifications.dto.OutboundRoutingEnvelope;

/**
 * Abstraction for a single delivery channel (SMS, Telegram, Email, Push).
 * Each implementation is a Spring bean and participates in the routing logic.
 */
public interface NotificationChannel {

    /**
     * Unique channel id: "sms", "telegram", "email", "push".
     */
    String id();

    /**
     * Priority: lower value = tried first.
     */
    default int priority() {
        return 20;
    }

    /**
     * Whether this channel can deliver to the given recipient envelope.
     * Evaluates if the envelope has active target destinations for this channel.
     */
    boolean supports(OutboundRoutingEnvelope envelope);

    /**
     * Deliver the message title and body to all destinations packed inside the envelope.
     *
     * @param envelope Transactional data container carrying routing targets and Thymeleaf texts
     * @return true on success if delivery conditions are met
     */
    boolean send(OutboundRoutingEnvelope envelope);
}