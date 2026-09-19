package com.soaesps.aggregator.config;

import com.soaesps.aggregator.actor.DeviceActor;
import com.soaesps.aggregator.actor.DeviceAdvisor;
import com.soaesps.aggregator.domain.MlMetricEvent;
import com.soaesps.aggregator.dto.DeviceDeps;
import com.soaesps.aggregator.repository.MetricsRepository;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.cluster.sharding.typed.ClusterShardingSettings;
import org.apache.pekko.cluster.sharding.typed.ShardingEnvelope;
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding;
import org.apache.pekko.cluster.sharding.typed.javadsl.Entity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Consumer;

@Configuration
public class PekkoConfiguration {

    /**
     * Creates the primary ActorSystem bean with explicit cluster configuration injected from Spring properties.
     */
    @Bean(destroyMethod = "terminate")
    public ActorSystem<Void> actorSystem() {
        return ActorSystem.create(Behaviors.empty(), "soaesps-aggregator", ConfigFactory.load());
    }

    /**
     * Extracts and exposes the ClusterSharding helper client from the active system.
     */
    @Bean
    public ClusterSharding clusterSharding(ActorSystem<Void> system) {
        return ClusterSharding.get(system);
    }

    /**
     * Orchestrates dynamic container dependency packaging required by actors upon cold-start triggers.
     */
    @Bean
    public DeviceDeps deviceDeps(MetricsRepository metricsRepository,
                                 DeviceAdvisor advisor,
                                 DeviceDeps.AdvicePublisher publisher) {

        // Inline implementation of the DeviceDeps.MetricsHistory functional interface via lambda
        DeviceDeps.MetricsHistory metricsBridge = (deviceId, limit) ->
                reactor.core.publisher.Mono.fromCallable(() -> {
                            // Call your existing repository method directly
                            List<MlMetricEvent> history = metricsRepository.recent(deviceId, limit);

                            // Reverse to deliver events to the actor queue in chronological order (old -> new)
                            return history.reversed();
                        })
                        .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic()) // Protects thread pools
                        .flatMapMany(reactor.core.publisher.Flux::fromIterable);

        return new DeviceDeps(metricsBridge, advisor, publisher);
    }

    /**
     * Initializes the shard region framework layout. Entities are generated lazily per deviceId.
     */
    @Bean
    public ActorRef<ShardingEnvelope<DeviceActor.Command>> deviceShardRegion(
            ActorSystem<Void> system,
            ClusterSharding sharding,
            DeviceDeps deeps) {
        return sharding.init(
                Entity.of(DeviceActor.ENTITY_TYPE_KEY,
                                ctx -> DeviceActor.create(ctx.getEntityId(), deeps))
                        .withSettings(ClusterShardingSettings.create(system))
        );
    }

    /**
     * Spring Cloud Stream binding channel. Routes incoming Kafka JSON telemetries
     * directly onto localized edge actors inside the sharding boundaries.
     */
    @Bean
    public Consumer<MlMetricEvent> telemetryIn(ClusterSharding sharding) {
        return event -> {
            if (event == null || event.deviceId() == null) return;

            sharding.entityRefFor(DeviceActor.ENTITY_TYPE_KEY, event.deviceId())
                    .tell(new DeviceActor.MetricReceived(event));
        };
    }
}