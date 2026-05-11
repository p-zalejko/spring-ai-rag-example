package com.example.demo.example_2.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;

@Data
@Builder
@Document(collection = "accounts")
public class Account {
    @Id
    private String id;
    private String tenantId;
    private String ownerId;
    private String ownerName;
    private String currency;
    private BigDecimal balance;
    private String accountType;   // CURRENT, SAVINGS, BUSINESS
}
