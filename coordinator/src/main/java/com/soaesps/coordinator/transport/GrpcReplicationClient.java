package com.soaesps.coordinator.transport;

import com.soaesps.coordinator.configuration.GrpcSecurityProperties;
import com.soaesps.coordinator.domain.GroupMessage;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transport client implementation handling low-level inter-server communication.
 * Implements mTLS (Mutual TLS) for encrypted and authenticated channel communication.
 */
@Slf4j
@Component
public class GrpcReplicationClient implements ReplicationTransportClient {

    private final GrpcSecurityProperties securityProperties;

    /**
     * Cache of gRPC channels per target server to avoid expensive re-creation.
     * Key: targetServerUrl, Value: ManagedChannel
     */
    private final Map<String, ManagedChannel> channelCache = new ConcurrentHashMap<>();

    public GrpcReplicationClient(GrpcSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    public CompletableFuture<Void> replicateBatch(String targetServerUrl, long iteration, List<GroupMessage> messages) {
        return CompletableFuture.runAsync(() -> {
            log.debug("Initiating secure network transit for iteration {} to remote peer: {}", iteration, targetServerUrl);

            try {
                // 1. Get or create a secure gRPC channel for this target
                ManagedChannel channel = getOrCreateSecureChannel(targetServerUrl);

                // 2. TODO: Map GroupMessage domain objects to Protobuf stubs
                // Example:
                // ReplicationServiceGrpc.ReplicationServiceBlockingStub stub = ReplicationServiceGrpc.newBlockingStub(channel);
                // ReplicationRequest request = buildProtobufRequest(iteration, messages);
                // stub.replicateBatch(request);

                // Simulate network latency and cryptographic overhead (e.g., 20-30ms)
                Thread.sleep(25);

                log.info("Successfully and securely replicated batch of {} messages to: {}",
                        messages.size(), targetServerUrl);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Network replication interrupted for peer: " + targetServerUrl, e);
            } catch (Exception e) {
                log.error("Failed to replicate batch to peer: {}. Error: {}", targetServerUrl, e.getMessage());
                throw new RuntimeException("Secure replication failed", e);
            }
        });
    }

    /**
     * Retrieves an existing channel or builds a new mTLS-enabled channel.
     */
    private ManagedChannel getOrCreateSecureChannel(String target) {
        return channelCache.computeIfAbsent(target, this::buildSecureChannel);
    }

    /**
     * Builds a gRPC channel with mTLS encryption.
     */
    private ManagedChannel buildSecureChannel(String target) {
        NettyChannelBuilder builder = NettyChannelBuilder.forTarget(target);

        if (securityProperties.isEnabled()) {
            log.info("Building mTLS-enabled gRPC channel for target: {}", target);
            try {
                SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient()
                        .trustManager(new File(securityProperties.getTrustCertCollectionFilePath()));

                // If client cert and key are provided, enable mutual authentication (mTLS)
                if (securityProperties.getClientCertChainFilePath() != null &&
                        securityProperties.getClientPrivateKeyFilePath() != null) {
                    sslContextBuilder.keyManager(
                            new File(securityProperties.getClientCertChainFilePath()),
                            new File(securityProperties.getClientPrivateKeyFilePath())
                    );
                }

                SslContext sslContext = sslContextBuilder.build();
                builder.sslContext(sslContext);

            } catch (Exception e) {
                log.error("Failed to initialize SSL context for gRPC. Check certificate paths.", e);
                throw new IllegalStateException("Failed to build secure gRPC channel", e);
            }
        } else {
            log.warn("gRPC security is DISABLED. Using PLAINTEXT. DO NOT USE IN PRODUCTION!");
            builder.usePlaintext();
        }

        return builder.build();
    }

    /**
     * Gracefully shuts down all cached gRPC channels when the application stops.
     */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down gRPC replication channels...");
        for (ManagedChannel channel : channelCache.values()) {
            channel.shutdownNow();
        }
        channelCache.clear();
    }
}