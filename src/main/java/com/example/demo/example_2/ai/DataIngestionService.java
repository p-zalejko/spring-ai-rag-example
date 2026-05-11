package com.example.demo.example_2.ai;

import com.example.demo.example_2.domain.Transaction;
import com.example.demo.example_2.domain.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Responsible for embedding transaction descriptions into the vector store.
 * <p>
 * In production this would be triggered by a Kafka event or a CDC change stream
 * whenever a new transaction is saved. Here we expose a simple method that
 * can be called from the DemoRunner on startup.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataIngestionService {

    private final TransactionRepository transactionRepository;
    private final VectorStore vectorStore;

    /**
     * Embed all transactions for the given tenant.
     * Each transaction's description becomes a vector Document with
     * metadata that allows per-tenant filtering during retrieval.
     */
    public void ingestTransactionsForTenant(String tenantId) {
        log.info("Starting ingestion for tenant={}", tenantId);

        List<Transaction> transactions = transactionRepository.findByTenantIdOrderByDateDesc(tenantId);

        List<Document> documents = transactions.stream()
                .map(t -> {
                    // The text that gets embedded — combine description with key fields
                    // for richer semantic matching
                    String content = String.format(
                            "Transaction on %s: %s of %s %s. Category: %s. Counterparty: %s. Description: %s",
                            t.getDate(), t.getType(), t.getAmount(), t.getCurrency(),
                            t.getCategory(), t.getCounterparty(), t.getDescription()
                    );

                    // Metadata stored alongside the vector — used for filtering and display
                    Map<String, Object> metadata = Map.of(
                            "tenantId", tenantId,
                            "transactionId", t.getId(),
                            "accountId", t.getAccountId(),
                            "date", t.getDate().toString(),
                            "category", t.getCategory(),
                            "type", t.getType(),
                            "amount", t.getAmount().toPlainString(),
                            "counterparty", t.getCounterparty()
                    );

                    return new Document(content, metadata);
                })
                .collect(Collectors.toList());

        // VectorStore.add() handles embedding generation + storage automatically
        vectorStore.add(documents);
        log.info("Ingested {} transaction documents for tenant={}", documents.size(), tenantId);
    }
}