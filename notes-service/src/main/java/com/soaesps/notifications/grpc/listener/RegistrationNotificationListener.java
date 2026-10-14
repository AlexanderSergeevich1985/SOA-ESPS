package com.soaesps.notifications.grpc.listener;

import com.soaesps.notifications.dto.OutboundRoutingEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RegistrationNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(RegistrationNotificationListener.class);
    // private final NotificationClient notificationClient; (Your Feign or gRPC client)

    @Async("taskExecutor") // Uses the async thread pool configuration we built earlier
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegisteredEvent(OutboundRoutingEnvelope envelope) {
        log.info("Database transaction committed. Dispatching registration message to userId: {}", envelope.userId());

        try {
            // Here you route the envelope to your notification sending pipeline (e.g., Feign, Kafka, or direct library)
            // notificationClient.send(envelope);
            log.info("Registration message successfully dispatched via channel: {}", envelope.channelType());
        } catch (Exception e) {
            log.error("Failed to send registration notification for userId: {}", envelope.userId(), e);
            // Handle retry logic or dead-letter-queue (DLQ) processing here
        }
    }
}