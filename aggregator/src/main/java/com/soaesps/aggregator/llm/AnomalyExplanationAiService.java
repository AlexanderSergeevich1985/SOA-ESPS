package com.soaesps.aggregator.llm;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * AI service that explains a single anomaly event in plain language.
 * Used by DeviceAdvisor to enrich high/medium severity advice messages.
 */
public interface AnomalyExplanationAiService {

    @SystemMessage("""
            You are an IoT support assistant. Explain shortly (1-2 sentences, Russian)
            what is wrong with the device and what the user should do right now.
            No markdown, no preamble.
            """)
    @UserMessage("""
            device_id: {{deviceId}}
            metric: {{metricName}}, value: {{value}}, anomaly_score: {{anomalyScore}}
            recent values (old -> new): {{window}}
            """)
    String explain(@V("deviceId") String deviceId,
                   @V("metricName") String metricName,
                   @V("value") double value,
                   @V("anomalyScore") double anomalyScore,
                   @V("window") String window);
}