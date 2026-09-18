package com.soaesps.aggregator.llm;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Pipeline responsible for populating the vector database (EmbeddingStore) with device manuals.
 * Runs asynchronously right after the Spring context is fully initialized.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagIngestionService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    /**
     * Automatically scans the specified folder for txt/md manuals and indexes them into RAG.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeKnowledgeBase() {
        log.info("Starting RAG knowledge base ingestion pipeline...");

        try {
            // Path where device troubleshooting guides and manuals are stored
            Path manualsPath = Paths.get("manuals");
            if (!Files.exists(manualsPath)) {
                Files.createDirectories(manualsPath);
                log.info("Created empty 'manuals' directory. Place device text files there for RAG initialization.");
                return;
            }

            // Define the segmentation strategy: chunks of 300 characters with 30 characters overlap
            DocumentSplitter splitter = DocumentSplitters.recursive(300, 30);

            // Build the ingestion manager combining storage, vector weights, and text splitting rules
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .documentSplitter(splitter)
                    .build();

            // Read all text files from the directory and ingest them
            Files.list(manualsPath)
                    .filter(path -> path.toString().endsWith(".txt") || path.toString().endsWith(".md"))
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);
                            // Wrap raw text into a LangChain4j Document model
                            Document document = Document.from(content);

                            ingestor.ingest(document);
                            log.info("Successfully ingested RAG document: {}", path.getFileName());
                        } catch (Exception ex) {
                            log.error("Failed to process RAG file: {}", path.getFileName(), ex);
                        }
                    });

            log.info("RAG knowledge base ingestion completed successfully.");
        } catch (Exception e) {
            log.error("Critical failure during RAG ingestion phase", e);
        }
    }
}