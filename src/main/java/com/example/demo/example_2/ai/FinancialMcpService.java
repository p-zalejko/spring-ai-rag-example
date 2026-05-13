package com.example.demo.example_2.ai;

import com.example.demo.example_2.domain.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialMcpService {

    private final TransactionRepository repo;

    @McpTool(
            name = "calculate_total_spending_for_the_last_year",
            description = "Calculates total spending for the last year.")
    public BigDecimal revenueLastMonth(@McpToolParam(description = "tenant id") String tenantId) {
        LocalDate start = LocalDate.now().minusYears(1).withDayOfMonth(1);
        LocalDate end = LocalDate.now();

        List<TransactionRepository.CategoryTotal> categoryTotals = repo.spendingByCategory(tenantId, start, end);
        double sum = categoryTotals.stream()
                .mapToDouble(c -> c.getTotal().doubleValue())
                .sum();
        return BigDecimal.valueOf(sum);
    }

    @McpTool(
            name = "calculate_spending_for_range",
            description = "Calculates s[pending for a given date range.")
    public BigDecimal revenueForRange(
            @McpToolParam(description = "Data from") String from,
            @McpToolParam(description = "Data to") String to,
            @McpToolParam(description = "tenant id") String tenantId
    ) {
        List<TransactionRepository.CategoryTotal> categoryTotals = repo.spendingByCategory(
                tenantId,
                LocalDate.parse(from),
                LocalDate.parse(to)
        );
        double sum = categoryTotals.stream()
                .mapToDouble(c -> c.getTotal().doubleValue())
                .sum();
        return BigDecimal.valueOf(sum);
    }
}
