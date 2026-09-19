package com.soaesps.aggregator.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.model.embedding.DisabledEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

/**
 * Infrastructure configuration for LLM Memory management and RAG components.
 */
@Configuration
public class LlmInfrastructureConfig {

    @Bean
    public TokenCountEstimatorFactory tokenCountEstimatorFactory() {
        return new TokenCountEstimatorFactory();
    }

    @Bean
    public TokenCountEstimator tokenCountEstimator(
            TokenCountEstimatorFactory factory,
            @Value("${langchain4j.chat-model.provider:open-ai}") String provider,
            @Value("${langchain4j.chat-model.model-name:gpt-4o-mini}") String modelName) {
        return factory.create(provider, modelName);
    }

    /**
     * Chat memory provider that adapts dynamically to whatever model is auto-configured by Spring.
     * Removes the hardcoded OpenAiTokenizer reference entirely.
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider(TokenCountEstimator tokenCountEstimator) {
        return memoryId -> TokenWindowChatMemory.withMaxTokens(2000, tokenCountEstimator);
    }

    /**
     * Vector database store for RAG (Device Manuals, Error Codes, Troubleshooting Guides).
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public EmbeddingModelFactory embeddingModelFactory() {
        return new EmbeddingModelFactory();
    }

    @Bean
    public EmbeddingModel embeddingModel(
            EmbeddingModelFactory factory,
            @Value("${langchain4j.embedding-model.provider:open-ai}") String provider,
            @Value("${langchain4j.embedding-model.model-name:text-embedding-3-small}") String modelName) {

        return factory.create(provider, modelName);
    }

    /**
     * Legacy RAG Retriever responsible for fetching relevant text segments based on user query/metrics.
     * Compatible with the compiled langchain4j-core version.
     */
    @Bean
    public ContentRetriever retriever(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel) {

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3) // Limits the retrieval to top 3 most relevant segments
                // Optional: .minScore(0.6) could be added here if supported by your version's builder
                .build();
    }

    /**
     * Produces a {@link TokenCountEstimator} that matches the chat model actually in use.
     *
     * <p>OpenAI-family models get the exact BPE tokenizer (encoding is picked
     * inside OpenAiTokenizer by model name: cl100k_base / o200k_base).
     * Providers without a bundled Java tokenizer (Ollama, local vLLM, etc.)
     * fall back to the chars/4 heuristic - precise enough for window trimming.
     */
    public static class TokenCountEstimatorFactory {

        /**
         * @param provider  provider id: "open-ai", "openai", "azure-openai", "ollama", ...
         * @param modelName chat model name; used by providers with model-specific encodings
         */
        public TokenCountEstimator create(String provider, String modelName) {
            String normalizedProvider = provider == null ? "" : provider.toLowerCase(Locale.ROOT);
            String normalizedModel = modelName == null ? "" : modelName.toLowerCase(Locale.ROOT);

            if (normalizedProvider.contains("open-ai") || normalizedProvider.contains("openai")) {
                if (normalizedModel.contains("gpt") || normalizedModel.contains("text-davinci")) {
                    return openAi(modelName);
                }
            }

            // Safe universal fallback for Qwen, Llama, and other custom models
            return heuristic();
        }

        /** Exact BPE tokenizer; OpenAiTokenizer already implements TokenCountEstimator. */
        public TokenCountEstimator openAi(String modelName) {
            return new OpenAiTokenCountEstimator(modelName);
        }

        /** ~4 chars per token: good enough for memory-window trimming on local/custom models. */
        public TokenCountEstimator heuristic() {
            return new TokenCountEstimator() {
                @Override
                public int estimateTokenCountInText(String text) {
                    return text == null || text.trim().isEmpty() ? 0 : (text.length() + 3) / 4;
                }

                @Override
                public int estimateTokenCountInMessage(ChatMessage chatMessage) {
                    if (chatMessage == null) return 0;
                    return estimateTokenCountInText(extractText(chatMessage));
                }

                @Override
                public int estimateTokenCountInMessages(Iterable<ChatMessage> iterable) {
                    if (iterable == null) return 0;
                    int totalTokens = 0;
                    for (ChatMessage message : iterable) {
                        if (message != null) {
                            totalTokens += estimateTokenCountInText(extractText(message));
                        }
                    }
                    return totalTokens;
                }
            };
        }

        private String extractText(ChatMessage message) {
            return switch (message) {
                case UserMessage userMessage ->
                        userMessage.hasSingleText() ? userMessage.singleText() : "";
                case AiMessage aiMessage -> aiMessage.text();
                case SystemMessage systemMessage -> systemMessage.text();
                case null, default -> "";
            };
        }
    }

    // =========================================================================
    // Embedding Model Factory
    // =========================================================================
    public static class EmbeddingModelFactory {

        /**
         * @param provider     provider id: "open-ai", "openai", "ollama", ...
         * @param modelName    embedding model name; used by provider-specific models
         */
        public EmbeddingModel create(String provider, String modelName) {
            String normalized = provider == null ? "" : provider.toLowerCase(java.util.Locale.ROOT).trim();

            return switch (normalized) {
                case "open-ai", "openai" -> openAi(modelName);
                default -> fallback();
            };
        }

        /** Exact OpenAI embedding model from the compiled langchain4j-open-ai module. */
        public EmbeddingModel openAi(String modelName) {
            return OpenAiEmbeddingModel.builder()
                    .modelName(modelName)
                    .build();
        }

        /** Safe No-Op fallback configuration from the core module to prevent context crash. */
        public EmbeddingModel fallback() {
            return new DisabledEmbeddingModel();
        }
    }
}