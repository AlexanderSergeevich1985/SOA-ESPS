package com.soaesps.aggregator.config;

import com.soaesps.aggregator.actor.DeviceActor;
import com.soaesps.aggregator.domain.MlMetricEvent;
import com.soaesps.aggregator.dto.DeviceDeps;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.cluster.sharding.typed.ShardingEnvelope;
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding;
import org.apache.pekko.cluster.sharding.typed.javadsl.Entity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class ShardingConfiguration {

    @Bean
    public ActorSystem<?> actorSystem() {
        return ActorSystem.create(Behaviors.empty(), "soaesps-aggregator");
    }

    /** Starts the shard region; entities are created lazily per deviceId. */
    @Bean
    public ActorRef<ShardingEnvelope<DeviceActor.Command>> deviceShardRegion(
            ActorSystem<Void> system,
            DeviceDeps deps) {
        return ClusterSharding.get(system).init(
                Entity.of(DeviceActor.ENTITY_TYPE_KEY,
                        ctx -> DeviceActor.create(ctx.getEntityId(), deps)));
    }

    /**
     * SCS entry point: every Kafka telemetry event is routed
     * to the actor responsible for that device.
     */
    @Bean
    public Consumer<MlMetricEvent> telemetryIn(ActorSystem<Void> system) {
        ClusterSharding sharding = ClusterSharding.get(system);
        return event -> sharding
                .entityRefFor(DeviceActor.ENTITY_TYPE_KEY, event.deviceId())
                .tell(new DeviceActor.MetricReceived(event));
    }
}