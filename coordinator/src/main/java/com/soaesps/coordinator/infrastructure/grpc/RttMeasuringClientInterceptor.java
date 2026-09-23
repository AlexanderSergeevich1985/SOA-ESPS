package com.soaesps.coordinator.infrastructure.grpc;

import com.soaesps.coordinator.algorithm.PredictionAlgorithm;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Production implementation of a gRPC ClientInterceptor.
 * Captures transport-level network latency and feeds it into the decoupled
 * prediction algorithm layer using thread-isolated local variables.
 */
@Component
public class RttMeasuringClientInterceptor implements ClientInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RttMeasuringClientInterceptor.class);

    // Injecting the interface, not the concrete implementation (Dependency Inversion Principle)
    private final PredictionAlgorithm predictionAlgorithm;

    public RttMeasuringClientInterceptor(PredictionAlgorithm predictionAlgorithm) {
        this.predictionAlgorithm = predictionAlgorithm;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Thread-isolated immutable snapshot allocation protecting from multi-stream rewrites
                final long startTimeNanos = System.nanoTime();

                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        try {
                            long durationNanos = System.nanoTime() - startTimeNanos;
                            double durationMs = durationNanos / 1_000_000.0;

                            // Guard against clock anomalies and ensure valid positive duration
                            if (durationMs >= 0.0 && !Double.isInfinite(durationMs)) {
                                // Direct non-blocking push interaction into the decoupled abstract filter layer
                                predictionAlgorithm.observeRtt(durationMs);
                                log.trace("gRPC invocation context closed. Observed transit delay: {} ms (Status: {})",
                                        durationMs, status.getCode());
                            }
                        } catch (Exception e) {
                            // Fail-safe: never let metric collection break the gRPC call lifecycle
                            log.error("Failed to parse and submit transport round-trip metrics downstream", e);
                        } finally {
                            // CRITICAL: Always propagate the onClose event to the original listener
                            super.onClose(status, trailers);
                        }
                    }
                }, headers);
            }
        };
    }
}