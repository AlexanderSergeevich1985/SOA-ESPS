package com.soaesps.aggregator.kafka;

import com.soaesps.aggregator.domain.MlMetricEvent;
import com.soaesps.aggregator.domain.Topics;
import com.soaesps.aggregator.repository.MetricsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Thin Kafka-to-DB adapter.
 *
 * <p>The actual insert logic lives in {@link MetricsRepository#writeBatch(List)};
 * this class only bridges the batch listener to the repository and commits
 * the consumer offset after a successful write (MANUAL_IMMEDIATE ack mode).
 *
 * <p>No internal buffer: Kafka itself provides batching through
 * {@code max.poll.records} / {@code fetch.min.bytes}.
 */
@Component
public class MetricsWriter {

    private static final Logger log = LoggerFactory.getLogger(MetricsWriter.class);

    private final MetricsRepository repository;

    public MetricsWriter(MetricsRepository repository) {
        this.repository = repository;
    }

    /**
     * Batch consumer entry point. Offset is committed only after all rows
     * are safely inserted, so at-least-once delivery is preserved.
     */
    @KafkaListener(topics = Topics.ML_METRICS, groupId = "aggregator-metrics-writer",
            containerFactory = "mlBatchListenerFactory")
    public void onBatch(List<MlMetricEvent> batch, Acknowledgment ack) {
        try {
            repository.writeBatch(batch);
            if (log.isDebugEnabled()) {
                log.debug("persisted {} metrics", batch.size());
            }
            ack.acknowledge();
        } catch (Exception e) {
            // Let the container redeliver; the offset stays uncommitted.
            log.error("failed to persist batch of {} metrics", batch.size(), e);
            throw e;
        }
    }

    /** Test-friendly synchronous write; identical to the happy path of {@link #onBatch}. */
    public void write(List<MlMetricEvent> batch) {
        repository.writeBatch(batch);
    }
}