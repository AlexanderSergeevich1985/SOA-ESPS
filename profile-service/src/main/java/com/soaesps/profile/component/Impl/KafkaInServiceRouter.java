package com.soaesps.profile.component.Impl;

import com.soaesps.core.DataModels.transaction.FailedOutboxEvent;
import com.soaesps.core.DataModels.user.UserInfo;
import com.soaesps.core.DataModels.user.UserProfile;
import com.soaesps.core.dto.BulkContactRegistrationMessage;
import com.soaesps.core.dto.UserDeletionMessage;
import com.soaesps.core.repository.transaction.FailedOutboxEventRepository;
import com.soaesps.profile.component.InServiceRouter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soaesps.core.dto.AuthRegistrationPayload;

import jakarta.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
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

@Primary
@Component
public class KafkaInServiceRouter implements InServiceRouter {

    private static final Logger logger = Logger.getLogger(KafkaInServiceRouter.class.getName());

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final FailedOutboxEventRepository failedOutboxRepository;
    private final TaskExecutor taskExecutor;
    private final ObjectMapper objectMapper;

    @Autowired
    @Lazy
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
        if (name == null || name.isBlank()) {
            return false;
        }

        final String routingKey = name;
        // Instantiate the standardized deletion message contract from the common library
        UserDeletionMessage deletionMessage = UserDeletionMessage.forUser(name);

        // Serialize the structural object container into an operational JSON block
        final String deleteEventJson;
        try {
            deleteEventJson = objectMapper.writeValueAsString(deletionMessage);
        } catch (Exception e) {
            throw new RuntimeException("SOA deletion contract infrastructure serialization failed", e);
        }

        // Register post-commit event publisher to protect RDBMS resources
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // Connection instance returned to the pool safely before triggering I/O streams
                taskExecutor.execute(() -> {

                    // Signal the auth cluster to instantly revoke credentials and tokens
                    kafkaTemplate.send("auth-registration-events", routingKey, deleteEventJson)
                            .whenComplete((result, ex) -> {
                                if (ex != null) {
                                    logger.log(Level.SEVERE, "Auth node cluster write failure on drop lifecycle. Moving to outbox.", ex);
                                    self.saveToFailedOutbox(routingKey, "AUTH_DELETION_FAILED", deleteEventJson);
                                } else {
                                    logger.log(Level.INFO, "Deletion event context dispatched to auth domain for key: " + routingKey);
                                }
                            });

                    // Signal the notes/notifications cluster to purge or archive related configurations
                    kafkaTemplate.send("user-notification-events", routingKey, deleteEventJson)
                            .whenComplete((result, ex) -> {
                                if (ex != null) {
                                    logger.log(Level.SEVERE, "Notes node cluster write failure on drop lifecycle. Moving to outbox.", ex);
                                    self.saveToFailedOutbox(routingKey, "BULK_NOTIFICATION_DELETION_FAILED", deleteEventJson);
                                } else {
                                    logger.log(Level.INFO, "Deletion event context dispatched to notes domain for key: " + routingKey);
                                }
                            });
                });
            }
        });

        return true;
    }

    @Override
    public boolean updateExistingUser(@NotNull final UserProfile profile) {
        final String routingKey = profile.getUserName();

        UserInfo userInfo = profile.getUserInfo();
        String email = userInfo != null ? userInfo.getEmail() : null;
        String telephone = userInfo != null ? userInfo.getTelephone() : null;

        BulkContactRegistrationMessage updateMessage = BulkContactRegistrationMessage.fromEmailAndPhone(
                profile.getUserName(),
                email,
                telephone
        );

        final String updateJson;
        try {
            updateJson = objectMapper.writeValueAsString(updateMessage);
        } catch (Exception e) {
            throw new RuntimeException("SOA infrastructure update contract serialization failed", e);
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                taskExecutor.execute(() -> {
                    kafkaTemplate.send("user-notification-events", routingKey, updateJson)
                            .whenComplete((result, ex) -> {
                                if (ex != null) {
                                    logger.log(Level.SEVERE, "Notes node cluster write drop experienced during update. Initiating fallback outbox routing.", ex);
                                    self.saveToFailedOutbox(routingKey, "BULK_NOTIFICATION_UPDATE_FAILED", updateJson);
                                } else {
                                    logger.log(Level.INFO, "Event context successfully routed via message broker for update domain: " + routingKey);
                                }
                            });
                });
            }
        });

        return true;
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
