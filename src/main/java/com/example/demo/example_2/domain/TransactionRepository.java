package com.example.demo.example_2.domain;

import lombok.Data;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {

    List<Transaction> findByTenantIdAndAccountIdAndDateBetweenOrderByDateDesc(
            String tenantId, String accountId, LocalDate from, LocalDate to);

    List<Transaction> findByTenantIdOrderByDateDesc(String tenantId);

    // Monthly spending by category (DEBIT only)
    @Aggregation(pipeline = {
            "{ $match: { tenantId: ?0, type: 'DEBIT', " +
                    "date: { $gte: ?1, $lte: ?2 } } }",
            "{ $group: { _id: '$category', total: { $sum: '$amount' } } }",
            "{ $sort: { total: -1 } }"
    })
    List<CategoryTotal> spendingByCategory(String tenantId, LocalDate from, LocalDate to);

    // Top counterparties by spend
    @Aggregation(pipeline = {
            "{ $match: { tenantId: ?0, type: 'DEBIT' } }",
            "{ $group: { _id: '$counterparty', total: { $sum: '$amount' } } }",
            "{ $sort: { total: -1 } }",
            "{ $limit: 5 }"
    })
    List<CounterpartyTotal> topCounterparties(String tenantId);

    // ── Projections ──────────────────────────────────────────────────────────
    @Data
    class CategoryTotal {
        String id;
        BigDecimal total;
    }

    @Data
    class CounterpartyTotal {
        String id;
        BigDecimal total;
    }
}