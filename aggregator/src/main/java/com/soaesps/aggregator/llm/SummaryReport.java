package com.soaesps.aggregator.llm;

import java.util.List;

/**
 * Structured output of the periodic report LLM call.
 * LangChain4j maps the model JSON response onto this record via a generated JSON schema.
 */
public record SummaryReport(
        String title,
        String summary,
        List<String> recommendations,
        Severity severity
) {
    public enum Severity { LOW, MEDIUM, HIGH }
}