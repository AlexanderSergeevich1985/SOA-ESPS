package com.soaesps.aggregator.llm;

import com.soaesps.aggregator.dto.AnomalyContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Production implementation of {@link LlmProcessor} using LangChain4j declarative AI services.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LangChain4jLlmProcessor implements LlmProcessor {

    private final AnomalyExplanationAiService anomalyAi;
    private final MetricsSummaryAiService summaryAi;

    @Override
    public String getProviderName() {
        return "langchain4j";
    }

    @Override
    public String processRealtimeAnomaly(AnomalyContext ac, String windowCsv) {
        log.debug("Executing real-time LLM call via LangChain4j for device={}", ac.deviceId());
        return anomalyAi.explain(ac.deviceId(), ac.metricName(), ac.value(), ac.anomalyScore(), windowCsv);
    }

    @Override
    public SummaryReport processPeriodicSummary(long userId, String metricsCsv) {
        log.debug("Executing periodic 6h summary LLM call via LangChain4j for user={}", userId);
        return summaryAi.summarize(userId, metricsCsv);
    }
}