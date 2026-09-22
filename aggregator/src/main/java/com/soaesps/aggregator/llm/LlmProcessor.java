package com.soaesps.aggregator.llm;

import com.soaesps.aggregator.dto.AnomalyContext;

/**
 * Strategy interface to decouple business logic from a specific LLM framework or provider.
 * Allows easy extension (e.g., switching from LangChain4j to Spring AI, Ollama, or adding Mocks).
 */
public interface LlmProcessor {

    /**
     * Supports either "langchain4j", "spring-ai", "mock", etc.
     */
    String getProviderName();

    /**
     * Translates a single critical real-time anomaly into a human-readable string.
     */
    String processRealtimeAnomaly(AnomalyContext context, String windowCsv);

    /**
     * Translates 6-hour metric aggregates into a structured user-facing report.
     */
    SummaryReport processPeriodicSummary(long userId, String metricsCsv);
}