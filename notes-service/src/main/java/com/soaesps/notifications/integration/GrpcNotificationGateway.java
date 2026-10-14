package com.soaesps.notifications.integration;

import com.soaesps.notifications.config.IntegrationConstant;
import com.soaesps.notifications.grpc.service.RegistrationRequest;
import com.soaesps.notifications.grpc.service.NotificationRequest;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import reactor.core.publisher.Mono;

@MessagingGateway
public interface GrpcNotificationGateway {

    /**
     * Sends a composite registration request to the integration pipeline and reactively awaits completion.
     * Spring Integration automatically bridges the response pipeline with this Mono.
     *
     * @param request the incoming gRPC user registration and welcome notification request
     * @return a Mono emitting completion once successfully ingested into the pipeline
     */
    @Gateway(requestChannel = IntegrationConstant.GRPC_REGISTRATION_INPUT_CHANNEL)
    Mono<Void> processRegistration(RegistrationRequest request);

    /**
     * Sends a standalone notification request to the integration pipeline and reactively awaits completion.
     * Spring Integration automatically bridges the response pipeline with this Mono.
     *
     * @param request the incoming gRPC standalone notification request
     * @return a Mono emitting completion once successfully ingested into the pipeline
     */
    @Gateway(requestChannel = IntegrationConstant.GRPC_NOTIFY_INPUT_CHANNEL)
    Mono<Void> processNotification(NotificationRequest request);
}
