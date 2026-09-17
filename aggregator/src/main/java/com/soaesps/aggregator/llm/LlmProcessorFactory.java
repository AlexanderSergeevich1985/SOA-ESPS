package com.soaesps.aggregator.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Factory that holds all available {@link LlmProcessor} strategies.
 * Switches implementations dynamically based on the configuration property.
 */
@Component
public class LlmProcessorFactory {

    private final Map<String, LlmProcessor> processors;
    private final String activeProvider;

    public LlmProcessorFactory(List<LlmProcessor> processorList,
                               @Value("${aggregator.llm.provider:langchain4j}") String activeProvider) {
        this.processors = processorList.stream()
                .collect(Collectors.toMap(p -> p.getProviderName().toLowerCase(), p -> p));
        this.activeProvider = activeProvider.toLowerCase();
    }

    /**
     * Returns the configured LLM strategy based on application.yml property.
     */
    public LlmProcessor getActiveProcessor() {
        LlmProcessor processor = processors.get(activeProvider);
        if (processor == null) {
            throw new IllegalArgumentException("Unknown LLM provider configured: " + activeProvider);
        }
        return processor;
    }
}