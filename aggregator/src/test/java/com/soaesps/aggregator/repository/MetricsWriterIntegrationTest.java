package com.soaesps.aggregator.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class MetricsWriterIntegrationTest {

    /**
     * TimescaleDB image extends PostgreSQL with the extension pre-installed.
     * Testcontainers manages the container lifecycle per test class.
     */
    @Container
    static final PostgreSQLContainer<?> TSDB = new PostgreSQLContainer<>(
            "timescale/timescaledb:latest-pg16")
            .withDatabaseName("metrics_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", TSDB::getJdbcUrl);
        registry.add("spring.datasource.username", TSDB::getUsername);
        registry.add("spring.datasource.password", TSDB::getPassword);
    }

    @Autowired private MetricsWriter writer;
    @Autowired private MetricsRepository repository;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void batchInsertAndAggregate() throws InterruptedException {
        // Given: 100 metrics for one device
        List<MlMetricEvent> batch = IntStream.range(0, 100)
                .mapToObj(i -> new MlMetricEvent(
                        Instant.now(),
                        "device-42", 1L, "temperature",
                        20.0 + i * 0.1, 0.3 + (i > 80 ? 0.6 : 0.0), "normal"))
                .toList();

        // When: write in batch mode
        writer.writeBatch(batch);

        // Then: raw table has all rows
        Integer rawCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ml_metrics WHERE device_id = 'device-42'", Integer.class);
        assertThat(rawCount).isEqualTo(100);

        // And: hot cache returns them
        List<MlMetricEvent> recent = repository.recent("device-42", 50);
        assertThat(recent).hasSize(50);

        // Continuous aggregate refresh is async; for tests, refresh manually
        jdbc.execute("CALL refresh_continuous_aggregate('ml_metrics_hourly', NULL, NULL)");

        // And: hourly aggregation is correct
        List<DeviceStats> stats = repository.aggregateByDevice("device-42", 1);
        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).sampleCount()).isEqualTo(100);
        assertThat(stats.get(0).maxAnomaly()).isEqualTo(0.9);
    }
}