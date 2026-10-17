package com.soaesps.notifications.config;

import com.soaesps.core.dto.ContactRegistrationRequest;
import com.soaesps.notifications.dto.InboundNotificationEvent;
import com.soaesps.notifications.grpc.service.RegistrationRequest;
import com.soaesps.notifications.service.UserContactService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;

import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration
public class GrpcRegistrationIntegrationConfiguration {

    private final UserContactService userContactService;

    public GrpcRegistrationIntegrationConfiguration(UserContactService userContactService) {
        this.userContactService = userContactService;
    }

    // =========================================================================
    // INBOUND REGISTRATION CHANNEL (REACTIVE FLUX SHIFT)
    // =========================================================================

    @Bean(IntegrationConstant.GRPC_REGISTRATION_INPUT_CHANNEL)
    public MessageChannel grpcRegistrationInputChannel() {
        return MessageChannels.flux().getObject();
    }

    // =========================================================================
    // REGISTRATION & NOTIFICATION PIPELINE FLOW (FLUX POWERED)
    // =========================================================================

    @Bean
    public IntegrationFlow grpcRegistrationFlow() {
        return IntegrationFlow.from(IntegrationConstant.GRPC_REGISTRATION_INPUT_CHANNEL)
                // Step 1: Extract pure gRPC RegistrationRequest payload from downstream messaging wrappers
                .<Object, RegistrationRequest>transform(payload -> {
                    if (payload instanceof Message<?> message) {
                        return (RegistrationRequest) message.getPayload();
                    }
                    return (RegistrationRequest) payload;
                })
                // Step 2: Trigger reactive persistence layer emitting a Flux sequence
                .<RegistrationRequest>handle((request, headers) -> {
                    try {
                        UUID userUuid = UUID.fromString(request.getUserId());

                        List<ContactRegistrationRequest> coreRequests = request.getContactDetailsList().stream()
                                .map(detail -> new ContactRegistrationRequest(
                                        detail.getType(),
                                        detail.getIsPrimary(),
                                        detail.getContactMetaMap()
                                ))
                                .toList();

                         userContactService.processBatchContactRegistration(userUuid, coreRequests).blockLast();
                                //.thenMany(Flux.just(request));
                        return request;

                    } catch (IllegalArgumentException e) {
                        // Directly throw the MessagingException so Spring Integration routes it to the error pipeline
                        throw new MessagingException("Registration pipeline failed: Invalid cross-domain User UUID format", e);
                    } catch (Exception e) {
                        throw new MessagingException("Registration batch pipeline failed due to downstream internal exception", e);
                    }
                })
                // Step 3: Map registration records stream into continuous InboundNotificationEvent sequence
                /*.<Object, InboundNotificationEvent>transform(payload -> {
                    RegistrationRequest cleanRequest;
                    if (payload instanceof Message<?> message) {
                        cleanRequest = (RegistrationRequest) message.getPayload();
                    } else if (payload instanceof RegistrationRequest req) {
                        cleanRequest = req;
                    } else {
                        throw new IllegalArgumentException("Pipeline data corruption: Unsupported payload type inside downstream transformation node: "
                                + (payload != null ? payload.getClass().getName() : "null"));
                    }
                    return this.mapRegistrationToInboundEvent(cleanRequest);
                })
                // Step 4: Split or stream straight into the reactive router bridge endpoint
                .channel(IntegrationConstant.REACTIVE_ROUTER_BRIDGE)*/
                .get();
    }

    /**
     * Converts inbound gRPC multi-contact registration envelope to a flat operational event.
     *
     * @param request the source gRPC message containing registration properties
     * @return an immutable InboundNotificationEvent bound to the global system identifier
     */
    private InboundNotificationEvent mapRegistrationToInboundEvent(RegistrationRequest request) {
        UUID userUuid = UUID.fromString(request.getUserId());

        Map<String, Object> parameters = new HashMap<>();
        if (request.hasNotificationDetails()) {
            parameters.putAll(request.getNotificationDetails().getTemplatePayloadMap());
        }

        String templateName = request.hasNotificationDetails()
                ? request.getNotificationDetails().getTemplateName()
                : "WELCOME_TEMPLATE";

        return new InboundNotificationEvent(
                UUID.randomUUID().toString(), // Tracking message envelope ID
                userUuid,                     // Global persistent cross-domain User identity
                templateName,                 // Target notification context template
                parameters                    // Dynamic metadata tokens
        );
    }
}