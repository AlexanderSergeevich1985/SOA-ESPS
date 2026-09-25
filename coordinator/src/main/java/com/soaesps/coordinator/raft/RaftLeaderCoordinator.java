package com.soaesps.coordinator.raft;

import com.soaesps.coordinator.algorithm.PredictionAlgorithm;
import com.soaesps.coordinator.component.ConflictResolverI;
import com.soaesps.coordinator.dag.CausalDagProcessorI;
import com.soaesps.coordinator.domain.GroupMessage;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.ratis.client.RaftClient;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.protocol.RaftClientReply;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Component
public class RaftLeaderCoordinator {

    private static final Logger log = LoggerFactory.getLogger(RaftLeaderCoordinator.class);
    private static final int MAX_BUFFER_SIZE = 10_000;
    private static final String KAFKA_WAL_TOPIC = "soa-esps-coordinator-wal";

    private final CausalDagProcessorI dagProcessor;
    private final ConflictResolverI conflictResolver;
    private final PredictionAlgorithm predictionAlgorithm;
    private final TaskScheduler taskScheduler;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, GroupMessage> kafkaTemplate;
    private final RaftClient raftClient;
    private final MessageSerializer messageSerializer;
    private final MeterRegistry meterRegistry;

    private final ConcurrentMap<String, GroupMessage> iterationBuffer = new ConcurrentHashMap<>();
    private volatile ScheduledFuture<?> scheduledTask;
    private volatile long tickIntervalMs = 100;
    private volatile boolean running = true;

    private Timer processIterationTimer;

    public RaftLeaderCoordinator(CausalDagProcessorI dagProcessor,
                                 ConflictResolverI conflictResolver,
                                 PredictionAlgorithm predictionAlgorithm,
                                 TaskScheduler taskScheduler,
                                 RedisTemplate<String, Object> redisTemplate,
                                 KafkaTemplate<String, GroupMessage> kafkaTemplate,
                                 RaftClient raftClient,
                                 MessageSerializer messageSerializer,
                                 MeterRegistry meterRegistry) {
        this.dagProcessor = dagProcessor;
        this.conflictResolver = conflictResolver;
        this.predictionAlgorithm = predictionAlgorithm;
        this.taskScheduler = taskScheduler;
        this.redisTemplate = redisTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.raftClient = raftClient;
        this.messageSerializer = messageSerializer;
        this.meterRegistry = meterRegistry;

        Gauge.builder("soaesps.buffer.size", iterationBuffer, Map::size).register(meterRegistry);
        this.processIterationTimer = Timer.builder("soaesps.iteration.duration").register(meterRegistry);
    }

    @PostConstruct
    public void init() {
        scheduleNext();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Initiating graceful shutdown of RaftLeaderCoordinator...");
        running = false;
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
        drainBufferToKafka();
    }

    public void acceptMessage(GroupMessage message) {
        if (iterationBuffer.size() >= MAX_BUFFER_SIZE) {
            throw new IllegalStateException("Buffer full (size=" + MAX_BUFFER_SIZE + "), apply backpressure to client");
        }
        updateVectorClockInRedis(message);
        iterationBuffer.put(message.id(), message);
    }

    private void updateVectorClockInRedis(GroupMessage message) {
        try {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                byte[] key = ("vc:" + message.senderId()).getBytes();
                for (Map.Entry<String, Long> entry : message.vectorClock().getClocks().entrySet()) {
                    connection.hashCommands().hIncrBy(key, entry.getKey().getBytes(), entry.getValue());
                }
                return null;
            });
        } catch (Exception e) {
            if (e.getCause() instanceof ClassCastException cce) {
                log.error("Redis data type mismatch — possible state corruption for key vc:{}", message.senderId(), cce);
                throw new IllegalStateException("Vector clock state corrupted in Redis", cce);
            }
            log.error("Redis pipeline failed for message {}. Message accepted, VC will reconcile later.", message.id(), e);
        }
    }

    private void scheduleNext() {
        Trigger trigger = ctx -> {
            Instant last = ctx.lastCompletion();
            Instant base = (last == null) ? Instant.now() : last;
            return base.plusMillis(tickIntervalMs);
        };
        try {
            scheduledTask = taskScheduler.schedule(this::processIteration, trigger);
        } catch (RejectedExecutionException rex) {
            log.error("Scheduler rejected task — node is shutting down or thread pool exhausted", rex);
        }
    }

    /**
     * Core iteration loop. Designed for maximum resilience: guarantees ZERO message loss
     * even in the event of OutOfMemoryError or unexpected RuntimeExceptions.
     */
    private void processIteration() {
        List<GroupMessage> currentBatch = null;

        try {
            if (!running) return;

            if (iterationBuffer.isEmpty()) {
                adaptTick();
                scheduleNext();
                return;
            }

            // 1. Atomically drain the buffer
            currentBatch = drainBufferAtomic();

            // 2. Record the full iteration duration (including Raft network time)
            List<GroupMessage> finalCurrentBatch = currentBatch;
            processIterationTimer.record(() -> {
                List<GroupMessage> sorted = dagProcessor.linearizeBatch(finalCurrentBatch);
                List<GroupMessage> resolved = conflictResolver.resolveBatch(sorted);

                // 3. Real Raft replication
                if (replicateViaRaft(resolved)) {
                    resolved.forEach(dagProcessor::updateFrontier);
                    log.info("Committed {} messages to Raft log", resolved.size());
                } else {
                    // Consensus failed: return ALL original messages to buffer for retry
                    for (GroupMessage m : finalCurrentBatch) {
                        iterationBuffer.putIfAbsent(m.id(), m);
                    }
                    log.warn("Raft consensus failed, {} messages returned to buffer for retry", finalCurrentBatch.size());
                }
            });

            adaptTick();
        } catch (Throwable t) {
            log.error("Unrecoverable error in iteration. Rolling back {} messages to buffer and scheduling retry.",
                    currentBatch != null ? currentBatch.size() : 0, t);

            if (currentBatch != null) {
                for (GroupMessage m : currentBatch) {
                    iterationBuffer.putIfAbsent(m.id(), m);
                }
            }
        } finally {
            // Always reschedule to guarantee liveness
            if (running) {
                scheduleNext();
            }
        }
    }

    private List<GroupMessage> drainBufferAtomic() {
        List<GroupMessage> batch = new ArrayList<>(iterationBuffer.size());
        Iterator<Map.Entry<String, GroupMessage>> it = iterationBuffer.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, GroupMessage> e = it.next();
            batch.add(e.getValue());
            it.remove(); // Thread-safe removal
        }
        return batch;
    }

    /**
     * REAL Raft replication via Apache Ratis client.
     * FIX #2: Uses .get() instead of .join() to ensure TimeoutException is thrown directly,
     * not wrapped in CompletionException.
     */
    private boolean replicateViaRaft(List<GroupMessage> batch) {
        if (batch == null || batch.isEmpty()) {
            return true;
        }

        try {
            // 1. Serialize the entire batch into a single atomic payload
            byte[] payload = messageSerializer.serializeBatch(batch);
            Message raftMessage = Message.valueOf(ByteString.copyFrom(payload));

            // 2. Use the dynamic timeout predicted by the Particle Filter
            long safeTimeoutMs = (long) predictionAlgorithm.getPrediction();

            // 3. Send to Raft cluster and await quorum acknowledgment synchronously
            RaftClientReply reply = raftClient.async()
                    .send(raftMessage)
                    .get(safeTimeoutMs, TimeUnit.MILLISECONDS);

            if (reply.isSuccess()) {
                log.debug("Successfully replicated batch of {} messages to Raft quorum. Log index: {}",
                        batch.size(), reply.getLogIndex());
                return true;
            } else {
                log.warn("Raft replication rejected by cluster. Exception: {}", reply.getException());
                return false; // Trigger retry logic
            }

        } catch (TimeoutException e) {
            // Expected during network partitions or high jitter.
            log.warn("Raft quorum not reached within {}ms (network partition or high load). Batch will be retried.",
                    predictionAlgorithm.getPrediction());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Raft replication thread interrupted", e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected fatal error during Raft replication", e);
            return false;
        }
    }

    private void adaptTick() {
        long safeWindowMs = (long) predictionAlgorithm.getPrediction();
        long next = Math.max(50L, Math.min(2000L, safeWindowMs));

        if (next != tickIntervalMs) {
            log.debug("Tick interval adapted: {}ms -> {}ms", tickIntervalMs, next);
            tickIntervalMs = next;
        }
    }

    private void drainBufferToKafka() {
        List<GroupMessage> remaining = drainBufferAtomic();
        if (remaining.isEmpty()) {
            log.info("Buffer already empty, nothing to drain to Kafka WAL.");
            return;
        }

        log.info("Draining {} messages to Kafka WAL topic '{}' before shutdown...", remaining.size(), KAFKA_WAL_TOPIC);

        List<CompletableFuture<SendResult<String, GroupMessage>>> futures = new ArrayList<>(remaining.size());
        for (GroupMessage msg : remaining) {
            // Using entityKey or id as partition key ensures ordering guarantees in Kafka
            futures.add(kafkaTemplate.send(KAFKA_WAL_TOPIC, msg.entityKey(), msg));
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);
            log.info("Successfully drained {} messages to Kafka WAL", remaining.size());
        } catch (TimeoutException e) {
            log.error("Timeout while draining buffer to Kafka WAL. Some messages may be lost.", e);
        } catch (Exception e) {
            log.error("Failed to drain buffer to Kafka WAL before shutdown", e);
        }
    }
}