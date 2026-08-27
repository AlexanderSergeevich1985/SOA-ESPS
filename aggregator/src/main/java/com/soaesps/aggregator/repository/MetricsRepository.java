package com.soaesps.aggregator.repository;

import com.soaesps.aggregator.domain.DeviceStats;
import com.soaesps.aggregator.domain.MlMetricEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MetricsRepository {
    private static final String INSERT_SQL = """
            INSERT INTO ml_metrics (ts, device_id, user_id, metric_name,
                                    value, anomaly_score, predicted_state)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbc;

    /** Writes a whole Kafka batch in one round-trip. */
    public void writeBatch(List<MlMetricEvent> batch) {
        if (batch.isEmpty()) return;

        int[][] results = jdbc.batchUpdate(INSERT_SQL, batch, batch.size(),
                (ps, e) -> {
                    ps.setTimestamp(1, Timestamp.from(e.timestamp()));
                    ps.setString(2, e.deviceId());
                    ps.setLong(3, e.userId());
                    ps.setString(4, e.metricName());
                    ps.setDouble(5, e.value());
                    ps.setDouble(6, e.anomalyScore());
                    ps.setString(7, e.predictedState());
                });

        if (log.isDebugEnabled()) {
            log.debug("Inserted {} metrics", results.length);
        }
    }

    /**
     * Hot cache for DeviceActor: last N raw events for a device.
     * Uses the idx_ml_metrics_device_ts index.
     */
    public List<MlMetricEvent> recent(String deviceId, int limit) {
        return jdbc.query("""
                SELECT ts, device_id, user_id, metric_name,
                       value, anomaly_score, predicted_state
                FROM ml_metrics
                WHERE device_id = ?
                ORDER BY ts DESC
                LIMIT ?
                """,
                this::mapEvent, deviceId, limit);
    }

    /**
     * Active devices seen in the last N hours — for PeriodicReport fan-out.
     */
    public List<DeviceRef> activeDevices(int hours) {
        return jdbc.query("""
                SELECT DISTINCT device_id, user_id
                FROM ml_metrics
                WHERE ts >= now() - make_interval(hours => ?)
                """,
                (rs, i) -> new DeviceRef(rs.getString("device_id"), rs.getLong("user_id")),
                hours);
    }

    /**
     * Aggregated stats per device/metric for the last N hours.
     * Reads from the `ml_metrics_hourly` continuous aggregate —
     * no on-the-fly aggregation of raw rows.
     */
    public List<DeviceStats> aggregateLastHours(int hours) {
        return jdbc.query("""
                SELECT device_id, user_id, metric_name,
                       SUM(sample_count) AS sample_count,
                       AVG(avg_value)    AS avg_value,
                       MIN(min_value)    AS min_value,
                       MAX(max_value)    AS max_value,
                       AVG(stddev_value) AS stddev_value,
                       MAX(max_anomaly)  AS max_anomaly,
                       MODE() WITHIN GROUP (ORDER BY dominant_state) AS dominant_state
                FROM ml_metrics_hourly
                WHERE bucket >= now() - make_interval(hours => ?)
                GROUP BY device_id, user_id, metric_name
                ORDER BY max_anomaly DESC NULLS LAST
                """,
                this::mapStats, hours);
    }

    public List<DeviceStats> aggregateByDevice(String deviceId, int hours) {
        return jdbc.query("""
                SELECT device_id, user_id, metric_name,
                       SUM(sample_count) AS sample_count,
                       AVG(avg_value), MIN(min_value), MAX(max_value),
                       AVG(stddev_value), MAX(max_anomaly),
                       MODE() WITHIN GROUP (ORDER BY dominant_state)
                FROM ml_metrics_hourly
                WHERE device_id = ?
                  AND bucket >= now() - make_interval(hours => ?)
                GROUP BY device_id, user_id, metric_name
                """,
                this::mapStats, deviceId, hours);
    }

    private MlMetricEvent mapEvent(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
        return new MlMetricEvent(
                rs.getTimestamp("ts").toInstant(),
                rs.getString("device_id"),
                rs.getLong("user_id"),
                rs.getString("metric_name"),
                rs.getDouble("value"),
                rs.getDouble("anomaly_score"),
                rs.getString("predicted_state"));
    }

    private DeviceStats mapStats(java.sql.ResultSet rs, int i) throws java.sql.SQLException {
        return new DeviceStats(
                rs.getString("device_id"),
                rs.getLong("user_id"),
                rs.getString("metric_name"),
                rs.getLong("sample_count"),
                rs.getDouble("avg_value"),
                rs.getDouble("min_value"),
                rs.getDouble("max_value"),
                rs.getDouble("stddev_value"),
                rs.getDouble("max_anomaly"),
                rs.getString("dominant_state"));
    }
}