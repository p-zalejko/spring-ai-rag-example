package com.example.demo.example_2.ai;

import com.example.demo.example_2.domain.TenantContext;
import com.example.demo.example_2.domain.Account;
import com.example.demo.example_2.domain.AccountRepository;
import com.example.demo.example_2.domain.Transaction;
import com.example.demo.example_2.domain.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinancialTools {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Tool(name = "list_accounts",
            description = "List all accounts for the current tenant with their balances and currencies.")
    public String listAccounts() {
        String tenantId = TenantContext.current();
        log.debug("Tool: list_accounts for tenant={}", tenantId);

        List<Account> accounts = accountRepository.findByTenantId(tenantId);
        if (accounts.isEmpty()) return "No accounts found.";

        return accounts.stream()
                .map(a -> String.format("Account[%s] %s | Type: %s | Balance: %s %s",
                        a.getId(), a.getOwnerName(), a.getAccountType(),
                        a.getBalance().toPlainString(), a.getCurrency()))
                .collect(Collectors.joining("\n"));
    }

    @Tool(name = "get_total_balance",
            description = "Get the total combined balance across all accounts for the current tenant.")
    public String getTotalBalance() {
        String tenantId = TenantContext.current();
        log.debug("Tool: get_total_balance for tenant={}", tenantId);

        BigDecimal total = accountRepository.sumBalanceByTenantId(tenantId)
                .orElse(BigDecimal.ZERO);
        return "Total balance across all accounts: " + total.toPlainString() + " (base currency)";
    }

    @Tool(name = "get_recent_transactions",
            description = "Get the 20 most recent transactions across all accounts for the current tenant.")
    public String getRecentTransactions() {
        String tenantId = TenantContext.current();
        log.debug("Tool: get_recent_transactions for tenant={}", tenantId);

        List<Transaction> txns = transactionRepository
                .findByTenantIdOrderByDateDesc(tenantId)
                .stream().limit(20).toList();

        if (txns.isEmpty()) return "No transactions found.";

        return txns.stream()
                .map(t -> String.format("[%s] %s %s %s | %s | %s | %s",
                        t.getDate(), t.getType(), t.getAmount().toPlainString(), t.getCurrency(),
                        t.getCategory(), t.getCounterparty(), t.getDescription()))
                .collect(Collectors.joining("\n"));
    }

    @Tool(name = "get_transactions_for_account",
            description = "Get transactions for a specific account between two dates. " +
                    "Dates must be in yyyy-MM-dd format. Example: accountId='acc-001', from='2024-01-01', to='2024-01-31'.")
    public String getTransactionsForAccount(String accountId, String from, String to) {
        String tenantId = TenantContext.current();
        log.debug("Tool: get_transactions_for_account tenant={} account={} from={} to={}", tenantId, accountId, from, to);

        LocalDate fromDate = LocalDate.parse(from, DateTimeFormatter.ISO_DATE);
        LocalDate toDate = LocalDate.parse(to, DateTimeFormatter.ISO_DATE);

        List<Transaction> txns = transactionRepository
                .findByTenantIdAndAccountIdAndDateBetweenOrderByDateDesc(
                        tenantId, accountId, fromDate, toDate);

        if (txns.isEmpty()) return "No transactions found for account " + accountId + " in that period.";

        return txns.stream()
                .map(t -> String.format("[%s] %s %s %s | %s | %s",
                        t.getDate(), t.getType(), t.getAmount().toPlainString(), t.getCurrency(),
                        t.getCategory(), t.getDescription()))
                .collect(Collectors.joining("\n"));
    }

    @Tool(name = "get_spending_by_category",
            description = "Get spending breakdown by category for a given month. " +
                    "Provide year as int (e.g. 2024) and month as int (e.g. 3 for March).")
    public String getSpendingByCategory(int year, int month) {
        String tenantId = TenantContext.current();
        log.debug("Tool: get_spending_by_category tenant={} year={} month={}", tenantId, year, month);

        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());

        List<TransactionRepository.CategoryTotal> totals =
                transactionRepository.spendingByCategory(tenantId, from, to);

        if (totals.isEmpty()) return "No spending data found for " + year + "-" + month;

        return totals.stream()
                .map(c -> String.format("%-20s %s", c.getId(), c.getTotal().toPlainString()))
                .collect(Collectors.joining("\n", "Category             Amount\n", ""));
    }

    @Tool(name = "get_top_counterparties",
            description = "Get the top 5 counterparties (merchants or payees) by total spending amount.")
    public String getTopCounterparties() {
        String tenantId = TenantContext.current();
        log.debug("Tool: get_top_counterparties for tenant={}", tenantId);

        List<TransactionRepository.CounterpartyTotal> tops =
                transactionRepository.topCounterparties(tenantId);

        if (tops.isEmpty()) return "No counterparty data found.";

        return tops.stream()
                .map(c -> String.format("%-30s %s", c.getId(), c.getTotal().toPlainString()))
                .collect(Collectors.joining("\n", "Counterparty                   Amount\n", ""));
    }
}