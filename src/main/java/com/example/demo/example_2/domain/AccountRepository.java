package com.example.demo.example_2.domain;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends MongoRepository<Account, String> {

    List<Account> findByTenantId(String tenantId);

    // Total balance across all accounts for tenant
    @Aggregation(pipeline = {
            "{ $match: { tenantId: ?0 } }",
            "{ $group: { _id: null, total: { $sum: '$balance' } } }"
    })
    Optional<BigDecimal> sumBalanceByTenantId(String tenantId);
}
