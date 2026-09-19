package com.soaesps.aggregator.activity;

import com.soaesps.aggregator.domain.DeviceRef;
import com.soaesps.aggregator.domain.DeviceStats;
import com.soaesps.aggregator.domain.Topics;
import com.soaesps.aggregator.domain.UserAdviceEvent;
import com.soaesps.aggregator.llm.LlmProcessorFactory;
import com.soaesps.aggregator.llm.SummaryReport;
import com.soaesps.aggregator.repository.MetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete implementation of Temporal ReportActivities.
 * Bridges Temporal worker execution threads with the pure JDBC MetricsRepository and extensible LLM factory.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportActivitiesImpl implements ReportActivities {

    private final MetricsRepository metricsRepository;
    private final LlmProcessorFactory llmFactory;
    private final KafkaTemplate<String, Object> adviceTemplate;

    @Override
    public List<DeviceRef> listActiveDevices() {
        log.info("Temporal Activity: Scanning active devices for the last 6 hours loop...");
        // Reuse your existing MetricsRepository method
        return metricsRepository.activeDevices(6);
    }

    @Override
    public List<DeviceStats> fetchStats(String deviceId, int windowHours) {
        log.debug("Temporal Activity: Fetching continuous aggregates for device={} (window={}h)", deviceId, windowHours);
        // Reuse your existing MetricsRepository method targeting the hourly view
        return metricsRepository.aggregateByDevice(deviceId, windowHours);
    }

    @Override
    public String askLlm(DeviceRef device, List<DeviceStats> stats) {
        log.info("Temporal Activity: Compiling 6h metrics aggregates for LLM analysis, user={}", device.userId());

        // Transform structured DeviceStats rows into a compact space-efficient CSV string to save prompt tokens
        String metricsCsv = stats.stream()
                .map(s -> String.join(" | ",
                        s.deviceId(),
                        s.metricName(),
                        String.format("%.2f", s.avg()),
                        String.format("%.2f", s.max()),
                        String.format("%.2f", s.maxAnomaly()),
                        String.valueOf(s.sampleCount())))
                .collect(Collectors.joining("\n"));

        // Execute via your flexible polymorphic LLM strategy processor
        SummaryReport report = llmFactory.getActiveProcessor().processPeriodicSummary(device.userId(), metricsCsv);

        // Return a compound metadata payload string (or JSON) for the workflow orchestrator to unpack
        return "%s|%s".formatted(report.severity().name(), report.summary());
    }

    @Override
    public void publishAdvice(Long userId, String deviceId, String advicePayload, String legacySeverity) {
        log.info("Temporal Activity: Dispatching batch summary report to Kafka for user={}", userId);

        String severity = legacySeverity;
        String message = advicePayload;

        // Safely unpack the structured payload if it contains the custom processor format separator
        if (advicePayload != null && advicePayload.contains("|")) {
            String[] parts = advicePayload.split("\\|", 2);
            severity = parts[0];
            message = parts[1];
        }

        UserAdviceEvent event = new UserAdviceEvent(
                UserAdviceEvent.TYPE_SUMMARY,
                UserAdviceEvent.TRIGGER_SCHEDULED, // Automated background batch kkind
                userId,
                deviceId,
                severity,
                message,
                Instant.now()
        );

        // Emit onto the user-advice channel partitioning streams strictly by userId key
        adviceTemplate.send(Topics.USER_ADVICE, String.valueOf(userId), event);
    }
}