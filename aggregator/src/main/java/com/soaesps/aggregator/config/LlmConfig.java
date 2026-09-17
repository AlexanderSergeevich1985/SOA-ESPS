package com.soaesps.aggregator.config;

import com.soaesps.aggregator.llm.AnomalyExplanationAiService;
import com.soaesps.aggregator.llm.MetricsSummaryAiService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires LangChain4j AI services on top of the auto-configured ChatModel
 * (provided by langchain4j-open-ai-spring-boot-starter from application.yml).
 */
@Configuration
public class LlmConfig {

    @Bean
    MetricsSummaryAiService metricsSummaryAiService(ChatModel chatModel) {
        return AiServices.builder(MetricsSummaryAiService.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    AnomalyExplanationAiService anomalyExplanationAiService(ChatModel chatModel) {
        return AiServices.builder(AnomalyExplanationAiService.class)
                .chatModel(chatModel)
                .build();
    }
}