package com.soaesps.notifications.grpc;

import com.soaesps.notifications.grpc.service.NotificationRequest;
import com.soaesps.notifications.grpc.service.NotificationResponse;
import com.soaesps.notifications.grpc.service.NotificationGrpcServiceGrpc;
import com.soaesps.notifications.grpc.service.RegistrationRequest;
import com.soaesps.notifications.integration.GrpcNotificationGateway;

import io.grpc.stub.ServerCallStreamObserver;
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
        log.info("Received gRPC notification request for template: {}", request.getNotificationDetails().getTemplateName());

        var serverCallObserver = (io.grpc.stub.ServerCallStreamObserver<NotificationResponse>) responseObserver;

        // Hand over execution to the Spring Integration pipeline
        integrationGateway.processNotification(request)
                .subscribe(
                        // Triggered when the message is successfully ingested into the channel
                        unused -> {},
                        // Triggered on ingestion errors (e.g., JSON parsing failure)
                        error -> {
                            log.error("Failed to queue gRPC notification request", error);
                            sendErrorResponse(serverCallObserver, error.getMessage());
                        },
                        // Triggered automatically on Mono<Void> completion
                        () -> {
                            log.info("gRPC notification request successfully ingested for template: {}", request.getNotificationDetails().getTemplateName());
                            sendSuccessResponse(serverCallObserver);
                        }
                );
    }

    /**
     *  Directly processes and routes a standalone notification without altering storage contexts.
     */
    @Override
    public void registerAndNotify(RegistrationRequest  request, StreamObserver<NotificationResponse> responseObserver) {
        log.info("Received gRPC registration and notification request for userId: {}", request.getUserId());

        var serverCallObserver = (ServerCallStreamObserver<NotificationResponse>) responseObserver;

        // Hand over execution to the Spring Integration pipeline supporting registration envelopes
        integrationGateway.processRegistration(request)
                .subscribe(
                        unused -> {},
                        error -> {
                            log.error("Failed to queue gRPC registration request for userId: {}", request.getUserId(), error);
                            sendErrorResponse(serverCallObserver, error.getMessage());
                        },
                        () -> {
                            log.info("gRPC registration request successfully ingested for userId: {}", request.getUserId());
                            sendSuccessResponse(serverCallObserver);
                        }
                );
    }

    private void sendSuccessResponse(ServerCallStreamObserver<NotificationResponse> observer) {
        if (!observer.isCancelled()) {
            NotificationResponse successResponse = NotificationResponse.newBuilder()
                    .setIsSuccess(true)
                    .setTrackingId(UUID.randomUUID().toString())
                    .build();
            observer.onNext(successResponse);
            observer.onCompleted();
        }
    }

    private void sendErrorResponse(ServerCallStreamObserver<NotificationResponse> observer, String errorMessage) {
        if (!observer.isCancelled()) {
            NotificationResponse errorResponse = NotificationResponse.newBuilder()
                    .setIsSuccess(false)
                    .setErrorMessage(errorMessage != null ? errorMessage : "Pipeline ingestion error")
                    .build();
            observer.onNext(errorResponse);
            observer.onCompleted();
        }
    }
}