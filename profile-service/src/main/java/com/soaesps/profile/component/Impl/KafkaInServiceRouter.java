package com.soaesps.profile.component.Impl;

import com.soaesps.core.DataModels.transaction.FailedOutboxEvent;
import com.soaesps.core.DataModels.user.UserInfo;
import com.soaesps.core.DataModels.user.UserProfile;
import com.soaesps.core.dto.BulkContactRegistrationMessage;
import com.soaesps.core.repository.FailedOutboxEventRepository;
import com.soaesps.profile.component.InServiceRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.core.dto.AuthRegistrationPayload;

import jakarta.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class KafkaInServiceRouter implements InServiceRouter {

    private static final Logger logger = Logger.getLogger(InServiceRouterImpl.class.getName());

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final FailedOutboxEventRepository failedOutboxRepository;
    private final TaskExecutor taskExecutor;
    private final ObjectMapper objectMapper;

    @Autowired
    private KafkaInServiceRouter self; // Self-proxy reference to execute REQUIRES_NEW transaction

    public KafkaInServiceRouter(KafkaTemplate<String, String> kafkaTemplate,
                               FailedOutboxEventRepository failedOutboxRepository,
                               TaskExecutor taskExecutor,
                               ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.failedOutboxRepository = failedOutboxRepository;
        this.taskExecutor = taskExecutor;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean createNewUser(@NotNull final UserProfile profile) {
        // Serialize the routing payload early to fail fast before transaction commitment hooks
        final String routingKey = profile.getUserName(); // Extract identity to use as partition key

        // Isolate and build distinct data contracts for each consumer domain
        assert profile.getUserDetails() != null;
        assert profile.getUserInfo() != null;
        AuthRegistrationPayload authPayload = new AuthRegistrationPayload(
                profile.getUserName(),
                profile.getUserDetails().getPassword(),      // Sensitive authentication fields
                profile.getUserDetails().isMfaEnabled(),
                profile.getUserInfo().getEmail(),
                profile.getUserInfo().getTelephone()
        );

        // Extract standard baseline contacts safely from nested UserInfo graph
        UserInfo userInfo = profile.getUserInfo();
        String email = userInfo != null ? userInfo.getEmail() : null;
        String telephone = userInfo != null ? userInfo.getTelephone() : null;

        // Instantiate the unified wrapper utilizing the shared common library factory mapping
        BulkContactRegistrationMessage bulkMessage = BulkContactRegistrationMessage.fromEmailAndPhone(
                profile.getUserName(),
                email,
                telephone
        );

        // Transform structured operational payloads into raw string parameters
        final String authJson;
        final String bulkContactsJson;
        try {
            authJson = objectMapper.writeValueAsString(authPayload);
            bulkContactsJson = objectMapper.writeValueAsString(bulkMessage);
        } catch (Exception e) {
            throw new RuntimeException("SOA infrastructure data contract serialization failed", e);
        }

        // Register post-commit synchronization hook
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // Database connection pool lock is already released.
                // Delegate high-latency network calls to the application thread manager.
                taskExecutor.execute(() -> {

                    // Dispatch credentials records block straight to the auth-service topology topic
                    kafkaTemplate.send("auth-registration-events", routingKey, authJson)
                            .whenComplete((result, ex) -> {
                                if (ex != null) {
                                    logger.log(Level.SEVERE, "Auth node cluster write drop experienced. Initiating fallback outbox routing.", ex);
                                    // Isolate storage of the failed state execution within its own physical context
                                    self.saveToFailedOutbox(routingKey, "AUTH_REGISTRATION_FAILED", authJson);
                                } else {
                                    logger.log(Level.INFO, "Event context successfully routed via message broker for auth domain: " + routingKey);
                                }
                            });

                    // Dispatch comprehensive notification criteria block straight to the notes-service topology topic
                    kafkaTemplate.send("user-notification-events", routingKey, bulkContactsJson)
                            .whenComplete((result, ex) -> {
                                if (ex != null) {
                                    logger.log(Level.SEVERE, "Notes node cluster write drop experienced. Initiating fallback outbox routing.", ex);
                                    // Isolate storage of the failed state execution within its own physical context
                                    self.saveToFailedOutbox(routingKey, "BULK_NOTIFICATION_SETUP_FAILED", bulkContactsJson);
                                } else {
                                    logger.log(Level.INFO, "Event context successfully routed via message broker for notes domain: " + routingKey);
                                }
                            });
                });
            }
        });

        return true;
    }

    @Override
    public boolean removeUser(String name) {
        return false;
    }

    @Override
    public UserDetails getUserDetailsByName(String name) throws IOException {
        return null;
    }

    /**
     * Creates an isolated historical outbox record when the message broker layer cannot accept the stream.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveToFailedOutbox(String aggregateId, String eventType, String payload) {
        FailedOutboxEvent event = new FailedOutboxEvent();
        event.setAggregateId(aggregateId);
        event.setEventType(eventType);
        event.setPayload(payload);
        this.failedOutboxRepository.save(event);
    }
}
