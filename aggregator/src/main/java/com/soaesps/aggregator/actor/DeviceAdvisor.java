package com.soaesps.aggregator.actor;

import com.soaesps.aggregator.domain.MlMetricEvent;
import com.soaesps.aggregator.llm.AnomalyExplanationAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Turns a window of recent metric events into a human-readable advice string.
 * This is the only place where LangChain4j is invoked.
 *
 * <p>Blocking LLM call is wrapped into {@link Mono#fromCallable} on the
 * bounded-elastic scheduler so Pekko dispatcher threads are never blocked.
 */
@Service
public class DeviceAdvisor {

    private static final Logger log = LoggerFactory.getLogger(DeviceAdvisor.class);

    private final AnomalyExplanationAiService ai;

    public DeviceAdvisor(AnomalyExplanationAiService ai) {
        this.ai = ai;
    }

    /**
     * Produces an advice message for the given device. Never throws:
     * if the LLM fails, returns a deterministic fallback template.
     */
    public Mono<String> adviseNow(String deviceId,
                                  List<MlMetricEvent> hotWindow,
                                  String severity) {
        return Mono.fromCallable(() -> explainOrFallback(deviceId, hotWindow, severity))
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic());
    }

    private String explainOrFallback(String deviceId,
                                     List<MlMetricEvent> window,
                                     String severity) {
        MlMetricEvent last = window.getLast();
        String windowCsv = window.stream()
                .map(e -> "%.2f".formatted(e.value()))
                .collect(Collectors.joining(","));
        try {
            return ai.explain(deviceId, last.metricName(),
                    last.value(), last.anomalyScore(), windowCsv);
        } catch (Exception e) {
            log.warn("LLM explanation failed for device={}, falling back", deviceId, e);
            return "Device %s shows %s-severity anomaly on %s (score %.2f). Please check it."
                    .formatted(deviceId, severity, last.metricName(), last.anomalyScore());
        }
    }
}