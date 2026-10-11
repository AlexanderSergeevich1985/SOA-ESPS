package com.soaesps.notifications.integration;

import com.soaesps.notifications.config.IntegrationConstant;
import com.soaesps.notifications.grpc.notification.NotificationRequest;
import org.springframework.integration.annotation.MessagingGateway;
import reactor.core.publisher.Mono;

@MessagingGateway(defaultRequestChannel = IntegrationConstant.GRPC_INPUT_CHANNEL)
public interface GrpcNotificationGateway {

    /**
     * Sends a request to the integration pipeline and reactively awaits the response.
     * Spring Integration automatically bridges the reply channel with this Mono.
     *
     * @param request the incoming gRPC notification request
     * @return a Mono emitting the notification response
     */
    Mono<Void> processNotification(NotificationRequest request);
}