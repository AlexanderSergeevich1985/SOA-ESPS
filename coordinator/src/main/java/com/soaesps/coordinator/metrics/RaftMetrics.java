package com.soaesps.coordinator.metrics;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class RaftMetrics {

    private final MeterRegistry meterRegistry;

    // Counters
    private final Counter raftCommitSuccess;
    private final Counter raftCommitFailure;
    private final Counter raftSnapshotCreated;

    // Gauges
    private final AtomicLong raftLogSize = new AtomicLong(0);
    private final AtomicLong raftCurrentTerm = new AtomicLong(0);
    private final AtomicLong bufferQueueSize = new AtomicLong(0);

    public RaftMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.raftCommitSuccess = Counter.builder("raft.commit.success")
                .description("Number of successful Raft commits")
                .register(meterRegistry);

        this.raftCommitFailure = Counter.builder("raft.commit.failure")
                .description("Number of failed Raft commits")
                .register(meterRegistry);

        this.raftSnapshotCreated = Counter.builder("raft.snapshot.created")
                .description("Number of Raft snapshots created")
                .register(meterRegistry);

        // Timers
        Timer raftReplicationTimer = Timer.builder("raft.replication.duration")
                .description("Time taken for Raft replication")
                .register(meterRegistry);

        Timer raftSnapshotTimer = Timer.builder("raft.snapshot.duration")
                .description("Time taken to create Raft snapshot")
                .register(meterRegistry);

        Timer causalSortTimer = Timer.builder("dag.causal.sort.duration")
                .description("Time taken for causal DAG sorting")
                .register(meterRegistry);

        // Register gauges
        Gauge.builder("raft.log.size", raftLogSize, AtomicLong::get)
                .description("Current Raft log size")
                .register(meterRegistry);

        Gauge.builder("raft.term.current", raftCurrentTerm, AtomicLong::get)
                .description("Current Raft term")
                .register(meterRegistry);

        Gauge.builder("buffer.queue.size", bufferQueueSize, AtomicLong::get)
                .description("Current inbound buffer size")
                .register(meterRegistry);
    }

    // Helper methods
    public void recordCommitSuccess() {
        raftCommitSuccess.increment();
    }

    public void recordCommitFailure() {
        raftCommitFailure.increment();
    }

    public void recordSnapshot() {
        raftSnapshotCreated.increment();
    }

    public void updateLogSize(long size) {
        raftLogSize.set(size);
    }

    public void updateTerm(long term) {
        raftCurrentTerm.set(term);
    }

    public void updateBufferSize(long size) {
        bufferQueueSize.set(size);
    }

    public Timer.Sample startReplicationTimer() {
        return Timer.start(meterRegistry);
    }

    public Timer.Sample startCausalSortTimer() {
        return Timer.start(meterRegistry);
    }
}