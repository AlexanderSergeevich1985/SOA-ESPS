package com.soaesps.profile.component.Impl;

import com.soaesps.core.DataModels.user.UserInfo;
import com.soaesps.core.DataModels.user.UserProfile;
import com.soaesps.core.dto.BulkContactRegistrationMessage;
import com.soaesps.core.dto.UserDeletionMessage;
import com.soaesps.core.service.transaction.OutboxService;
import com.soaesps.core.service.transaction.FastEventPublisher;
import com.soaesps.profile.component.InServiceRouter;
import com.soaesps.core.dto.AuthRegistrationPayload;

import jakarta.validation.constraints.NotNull;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

@Import({OutboxService.class, FastEventPublisher.class})
@Primary
@Component
public class KafkaInServiceRouter implements InServiceRouter {
    private static final String AUTH_REGISTER_TOPIC = "auth-registration-events";
    private static final String NOTIFICATION_EVENTS_TOPIC = "user-notification-events";

    private static final Logger logger = Logger.getLogger(KafkaInServiceRouter.class.getName());

    private final OutboxService outboxService;
    private final FastEventPublisher eventPublisher;

    public KafkaInServiceRouter(OutboxService outboxService,
                                FastEventPublisher eventPublisher) {
        this.outboxService = outboxService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean createNewUser(@NotNull final UserProfile profile) {
        // Serialize the routing payload early to fail fast before transaction commitment hooks
        final String routingKey = profile.getUserName(); // Extract identity to use as partition key

        Objects.requireNonNull(profile.getUserDetails(), "UserDetails cannot be null");
        Objects.requireNonNull(profile.getUserInfo(), "UserInfo cannot be null");

        // Isolate and build distinct data contracts for each consumer domain
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

        outboxService.saveAndPublishAfterCommit(routingKey, "AUTH_REGISTRATION", AUTH_REGISTER_TOPIC, authPayload);
        eventPublisher.publishAfterCommit(NOTIFICATION_EVENTS_TOPIC, routingKey, bulkMessage);

        return true;
    }

    @Override
    public boolean removeUser(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }

        // Instantiate the standardized deletion message contract from the common library
        UserDeletionMessage deletionMessage = UserDeletionMessage.forUser(name);

        outboxService.saveAndPublishAfterCommit(name, "AUTH_DEREGISTRATION", AUTH_REGISTER_TOPIC, deletionMessage);
        eventPublisher.publishAfterCommit(NOTIFICATION_EVENTS_TOPIC, name, deletionMessage);

        return true;
    }

    @Override
    public boolean updateExistingUser(@NotNull final UserProfile profile) {
        final String routingKey = profile.getUserName();

        Objects.requireNonNull(profile.getUserDetails(), "UserDetails cannot be null");
        Objects.requireNonNull(profile.getUserInfo(), "UserInfo cannot be null");

        // Isolate and build distinct data contracts for each consumer domain
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

        outboxService.saveAndPublishAfterCommit(routingKey, "AUTH_UPDATE_REGISTRATION", AUTH_REGISTER_TOPIC, authPayload);
        eventPublisher.publishAfterCommit(NOTIFICATION_EVENTS_TOPIC, routingKey, bulkMessage);

        return true;
    }

    @Override
    public UserDetails getUserDetailsByName(String name) throws IOException {
        return null;
    }
}
