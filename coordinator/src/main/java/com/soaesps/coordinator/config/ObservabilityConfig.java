package com.soaesps.coordinator.config;

import com.soaesps.coordinator.metrics.RaftMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

    /**
     * JVM and system metrics
     */
    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * Custom metrics for Raft consensus
     */
    @Bean
    public RaftMetrics raftMetrics(MeterRegistry meterRegistry) {
        return new RaftMetrics(meterRegistry);
    }
}