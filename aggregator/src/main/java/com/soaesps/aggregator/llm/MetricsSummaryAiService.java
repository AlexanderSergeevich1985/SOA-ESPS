package com.soaesps.aggregator.llm;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * AI service that turns 6-hour metric aggregates into a user-facing summary.
 * Instantiated as a Spring bean in {@link com.soaesps.aggregator.config.LlmConfig}.
 */
public interface MetricsSummaryAiService {

    @SystemMessage("""
            You are a telemetry analyst for IoT devices of the SOA-ESPS platform.
            You receive per-user device metric aggregates for the last 6 hours.
            Respond strictly according to the JSON schema of SummaryReport.
            Write summary and recommendations, at most 5 sentences total.
            Derive severity from the max anomaly score: <0.3 LOW, 0.3..0.6 MEDIUM, >0.6 HIGH.
            """)
    @UserMessage("""
            user_id: {{userId}}
            Aggregates (device | metric | avg | max | anomaly_avg | samples):
            {{metricsCsv}}
            """)
    SummaryReport summarize(@V("userId") long userId,
                            @V("metricsCsv") String metricsCsv);
}