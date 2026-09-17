package com.soaesps.aggregator.report;

import com.soaesps.aggregator.domain.Topics;
import com.soaesps.aggregator.domain.UserAdviceEvent;
import com.soaesps.aggregator.llm.LlmProcessorFactory;
import com.soaesps.aggregator.llm.SummaryReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Every 6 hours: reads continuous aggregates from TimescaleDB, asks the LLM
 * (LangChain4j) for a per-user summary and publishes it to [user-advice].
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PeriodicReportService {

    private static final String USER_IDS_SQL = """
            select distinct user_id from ml_metrics_1h
            where bucket > now() - interval '6 hours'
            """;

    private static final String ROWS_SQL = """
            select device_id, metric_name,
                   avg(avg_value)   as avg_value,
                   max(max_value)   as max_value,
                   avg(avg_anomaly) as avg_anomaly,
                   max(max_anomaly) as max_anomaly,
                   sum(samples)     as samples
            from ml_metrics_1h
            where bucket > now() - interval '6 hours' and user_id = ?
            group by device_id, metric_name
            order by max_anomaly desc
            limit 50
            """;

    private record AggRow(String deviceId, String metricName,
                          double avgValue, double maxValue,
                          double avgAnomaly, double maxAnomaly,
                          long samples) {}

    private static final RowMapper<AggRow> AGG_ROW_MAPPER = (rs, i) -> new AggRow(
            rs.getString("device_id"),
            rs.getString("metric_name"),
            rs.getDouble("avg_value"),
            rs.getDouble("max_value"),
            rs.getDouble("avg_anomaly"),
            rs.getDouble("max_anomaly"),
            rs.getLong("samples"));

    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String, Object> adviceTemplate;
    private final LlmProcessorFactory llmFactory;
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @Scheduled(cron = "${aggregator.report.cron:0 0 */6 * * *}")
    public void generateReports() {
        for (Long userId : jdbc.queryForList(USER_IDS_SQL, Long.class)) {
            List<AggRow> rows = jdbc.query(ROWS_SQL, AGG_ROW_MAPPER, userId);
            if (rows.isEmpty()) {
                continue;
            }
            SummaryReport report = summarizeSafe(userId, rows);
            adviceTemplate.send(Topics.USER_ADVICE, String.valueOf(userId),
                    new UserAdviceEvent(UserAdviceEvent.TYPE_SUMMARY, userId, null,
                            report.severity().name(), report.summary(), Instant.now()));
        }
    }

    /** LLM call with a deterministic fallback: a dead LLM must never kill the scheduler. */
    private SummaryReport summarizeSafe(Long userId, List<AggRow> rows) {
        try {
            return llmFactory.getActiveProcessor().processPeriodicSummary(userId, toCsv(rows));
        } catch (Exception e) {
            log.warn("LLM summarization failed for user={}, falling back to template", userId, e);
            return fallbackReport(rows);
        }
    }
    /** Compact CSV keeps the prompt small (roughly 1-2K tokens per user). */
    private String toCsv(List<AggRow> rows) {
        return rows.stream()
                .map(r -> String.join(" | ", r.deviceId(), r.metricName(),
                        fmt(r.avgValue()), fmt(r.maxValue()),
                        fmt(r.avgAnomaly()), String.valueOf(r.samples())))
                .collect(Collectors.joining("\n"));
    }

    private String fmt(Object d) {
        return d == null ? "-" : String.format("%.2f", ((Number) d).doubleValue());
    }

    private SummaryReport fallbackReport(List<AggRow> rows) {
        double maxAnomaly = rows.stream().mapToDouble(AggRow::maxAnomaly).max().orElse(0);
        SummaryReport.Severity severity = maxAnomaly > 0.6
                ? SummaryReport.Severity.HIGH
                : maxAnomaly > 0.3 ? SummaryReport.Severity.MEDIUM
                : SummaryReport.Severity.LOW;
        return new SummaryReport(
                "6-hour report",
                "Devices reported: %d, max anomaly score: %s.".formatted(rows.size(), fmt(maxAnomaly)),
                List.of("Check the devices with the highest anomaly score."),
                severity);
    }
}