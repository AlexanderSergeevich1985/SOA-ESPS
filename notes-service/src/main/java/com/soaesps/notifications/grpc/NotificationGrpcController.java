package com.soaesps.notifications.grpc;

import com.soaesps.notifications.grpc.notification.NotificationRequest;
import com.soaesps.notifications.grpc.notification.NotificationResponse;
import com.soaesps.notifications.grpc.notification.NotificationGrpcServiceGrpc;
import com.soaesps.notifications.integration.GrpcNotificationGateway;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

@GrpcService
public class NotificationGrpcController extends NotificationGrpcServiceGrpc.NotificationGrpcServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(NotificationGrpcController.class);

    private final GrpcNotificationGateway integrationGateway;

    public NotificationGrpcController(GrpcNotificationGateway integrationGateway) {
        this.integrationGateway = integrationGateway;
    }

    @Override
    public void sendNotification(NotificationRequest request, StreamObserver<NotificationResponse> responseObserver) {
        log.info("Received gRPC notification request for template: {}", request.getTemplateName());

        var serverCallObserver = (io.grpc.stub.ServerCallStreamObserver<NotificationResponse>) responseObserver;

        // Hand over execution to the Spring Integration pipeline
        integrationGateway.processNotification(request)
                .subscribe(
                        // Triggered when the message is successfully ingested into the channel
                        unused -> {},
                        // Triggered on ingestion errors (e.g., JSON parsing failure)
                        error -> {
                            log.error("Failed to queue gRPC notification request", error);
                            if (!serverCallObserver.isCancelled()) {
                                NotificationResponse errorResponse = NotificationResponse.newBuilder()
                                        .setIsSuccess(false)
                                        .setErrorMessage(error.getMessage() != null ? error.getMessage() : "Pipeline ingestion error")
                                        .build();
                                serverCallObserver.onNext(errorResponse);
                                serverCallObserver.onCompleted();
                            }
                        },
                        // Triggered automatically on Mono<Void> completion
                        () -> {
                            log.info("gRPC notification request successfully ingested for template: {}", request.getTemplateName());
                            if (!serverCallObserver.isCancelled()) {
                                NotificationResponse successResponse = NotificationResponse.newBuilder()
                                        .setIsSuccess(true)
                                        .setTrackingId(UUID.randomUUID().toString())
                                        .build();
                                serverCallObserver.onNext(successResponse);
                                serverCallObserver.onCompleted();
                            }
                        }
                );
    }
}