package com.soaesps.aggregator.config;

import com.soaesps.aggregator.domain.MlMetricEvent;
import com.soaesps.aggregator.repository.MetricsRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Consumer;

@Configuration
public class MetricsIngestionConfiguration {
    @Bean
    public Consumer<List<MlMetricEvent>> metricsIn(MetricsRepository writer) {
        // SCS batch-mode: Kafka records arrive as List, one DB round-trip per batch
        return writer::writeBatch;
    }
}