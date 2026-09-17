package com.soaesps.aggregator.config;

import com.soaesps.aggregator.actor.DeviceActor;
import com.soaesps.aggregator.dto.DeviceDeps;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.cluster.sharding.typed.ClusterShardingSettings;
import org.apache.pekko.cluster.sharding.typed.javadsl.ClusterSharding;
import org.apache.pekko.cluster.sharding.typed.javadsl.Entity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActorConfig {

    @Bean(destroyMethod = "terminate")
    public ActorSystem<Void> actorSystem(DeviceDeps deps) {
        return ActorSystem.create(Behaviors.setup(ctx -> {
            ClusterSharding.get(ctx.getSystem()).init(
                    Entity.of(DeviceActor.ENTITY_TYPE_KEY,
                                    ec -> DeviceActor.create(ec.getEntityId(), deps))
                            .withSettings(ClusterShardingSettings.create(ctx.getSystem())));
            return Behaviors.empty();
        }), "aggregator");
    }

    @Bean
    public DeviceDeps deviceDeps(DeviceDeps.MetricsHistory metrics,
                                 com.soaesps.aggregator.actor.DeviceAdvisor advisor,
                                 DeviceDeps.AdvicePublisher publisher) {
        return new DeviceDeps(metrics, advisor, publisher);
    }

    @Bean
    public ClusterSharding clusterSharding(ActorSystem<Void> system) {
        return ClusterSharding.get(system);
    }
}