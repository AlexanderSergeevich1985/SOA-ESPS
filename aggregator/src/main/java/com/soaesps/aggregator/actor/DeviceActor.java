package com.soaesps.aggregator.actor;

import com.soaesps.aggregator.domain.MlMetricEvent;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.apache.pekko.cluster.sharding.typed.javadsl.EntityTypeKey;

import java.util.*;

public final class DeviceActor extends AbstractBehavior<DeviceActor.Command> {

    public static final EntityTypeKey<Command> ENTITY_TYPE_KEY =
            EntityTypeKey.create(Command.class, "DeviceActor");

    private static final int HOT_WINDOW = 50;
    private static final double CRITICAL_SCORE = 0.9;
    private static final double TREND_SCORE = 0.7;
    private static final int TREND_LENGTH = 3;

    public sealed interface Command {}
    public record MetricReceived(MlMetricEvent event) implements Command {}
    record HistoryLoaded(List<MlMetricEvent> events) implements Command {}
    record AdviceSent(String id) implements Command {}

    private final String deviceId;
    private final DeviceDeps deps;
    private final Deque<MlMetricEvent> hot = new ArrayDeque<>(HOT_WINDOW);
    private boolean adviceInFlight = false;

    public static Behavior<Command> create(String deviceId, DeviceDeps deps) {
        return Behaviors.setup(ctx -> {
            DeviceActor actor = new DeviceActor(ctx, deviceId, deps);
            // Warm up hot cache from TimescaleDB. State is fully reconstructible
            // from the DB, so passivation is free and postStop persists nothing.
            deps.metrics().recent(deviceId, HOT_WINDOW)
                    .subscribe(events -> ctx.getSelf().tell(new HistoryLoaded(events)));
            return actor;
        });
    }

    private DeviceActor(ActorContext<Command> ctx, String deviceId, DeviceDeps deps) {
        super(ctx);
        this.deviceId = deviceId;
        this.deps = deps;
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(MetricReceived.class, this::onMetric)
                .onMessage(HistoryLoaded.class, m -> { m.events().forEach(hot::addLast); trim(); return this; })
                .onMessage(AdviceSent.class, m -> { adviceInFlight = false; return this; })
                .build();
    }

    private Behavior<Command> onMetric(MetricReceived cmd) {
        hot.addLast(cmd.event());
        trim();

        // Realtime decision: only critical situations trigger an immediate LLM call.
        // Routine events are left to the 6h batch path — no LLM spam.
        if (!adviceInFlight) {
            decisionFor(cmd.event()).ifPresent(severity -> {
                adviceInFlight = true;
                deps.advisor().adviseNow(deviceId, List.copyOf(hot), severity)
                        .subscribe(id -> context().getSelf().tell(new AdviceSent(id)));
            });
        }
        return this;
    }

    private void trim() {
        while (hot.size() > HOT_WINDOW) hot.pollFirst();
    }

    /** Decision policy: single critical event OR sustained upward trend. */
    private Optional<String> decisionFor(MlMetricEvent e) {
        if (e.anomalyScore() >= CRITICAL_SCORE) return Optional.of("high");
        if (risingTrend()) return Optional.of("medium");
        return Optional.empty();
    }

    private boolean risingTrend() {
        if (hot.size() < TREND_LENGTH) return false;
        var tail = new ArrayList<>(hot).subList(hot.size() - TREND_LENGTH, hot.size());
        return tail.stream().allMatch(m -> m.anomalyScore() >= TREND_SCORE);
    }
}