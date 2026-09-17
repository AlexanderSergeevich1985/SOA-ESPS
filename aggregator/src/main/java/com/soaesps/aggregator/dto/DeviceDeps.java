package com.soaesps.aggregator.dto;

import com.soaesps.aggregator.actor.DeviceAdvisor;
import com.soaesps.aggregator.domain.MlMetricEvent;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Reactive dependencies injected into {@link DeviceActor}.
 * Isolates the actor from Spring, JDBC and Kafka so the actor stays
 * pure Pekko code that can be unit-tested with a fake implementation.
 */
public record DeviceDeps(
        MetricsHistory metrics,
        DeviceAdvisor advisor,
        AdvicePublisher publisher
) {
    /** Reads recent events from TimescaleDB for actor warm-up. */
    public interface MetricsHistory {
        reactor.core.publisher.Flux<MlMetricEvent> recent(String deviceId, int limit);
    }

    /** Publishes the final advice to Kafka [user-advice]. */
    public interface AdvicePublisher {
        Mono<String> publish(long userId, String deviceId, String severity, String message);
    }
}