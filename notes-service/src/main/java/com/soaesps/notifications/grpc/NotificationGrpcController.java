package com.soaesps.notifications.grpc;

import com.soaesps.notifications.grpc.notification.NotificationRequest;
import com.soaesps.notifications.grpc.notification.NotificationResponse;
import com.soaesps.notifications.grpc.notification.NotificationGrpcServiceGrpc; // Generated class
import com.soaesps.notifications.integration.GrpcNotificationGateway;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        // Hand over execution to the Spring Integration pipeline
        integrationGateway.processNotification(request)
                .subscribe(
                        // Successful response from the pipeline
                        response -> {
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        // Error inside the pipeline
                        error -> {
                            log.error("Integration pipeline failed for gRPC request", error);

                            NotificationResponse errorResponse = NotificationResponse.newBuilder()
                                    .setIsSuccess(false)
                                    .setErrorMessage(error.getMessage() != null ? error.getMessage() : "Unknown pipeline error")
                                    .build();

                            responseObserver.onNext(errorResponse);
                            responseObserver.onCompleted();
                        }
                );
    }
}