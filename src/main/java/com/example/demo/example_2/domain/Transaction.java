package com.example.demo.example_2.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id;
    private String tenantId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String type;          // DEBIT, CREDIT
    private String category;      // FOOD, TRAVEL, SALARY, TRANSFER, etc.
    private String description;   // free text — this gets embedded for vector search
    private LocalDate date;
    private String counterparty;
}