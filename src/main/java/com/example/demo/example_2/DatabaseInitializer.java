package com.example.demo.example_2;

import com.example.demo.example_2.domain.Account;
import com.example.demo.example_2.domain.AccountRepository;
import com.example.demo.example_2.domain.Transaction;
import com.example.demo.example_2.domain.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs once at application startup.
 * Seeds two tenants with realistic accounts and transactions,
 * then embeds all transaction descriptions into the vector store.
 * <p>
 * Safe to restart — skips seeding if data already exists.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class DatabaseInitializer {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // ── Tenant definitions ────────────────────────────────────────────────
    private static final String TENANT_ACME = "tenant-acme";
    private static final String TENANT_BETA = "tenant-beta";

    @PostConstruct
    void run() {
        log.info("=== DatabaseInitializer starting ===");

        initTenant(TENANT_ACME, "Jan Kowalski", "Anna Kowalska");
        initTenant(TENANT_BETA, "Piotr Nowak", "Maria Nowak");

        log.info("=== DatabaseInitializer complete ===");
    }

    // ─────────────────────────────────────────────────────────────────────
    private void initTenant(String tenantId, String primaryOwner, String secondaryOwner) {
        if (!accountRepository.findByTenantId(tenantId).isEmpty()) {
            log.info("Tenant [{}] already seeded — skipping.", tenantId);
            return;
        }

        log.info("Seeding tenant [{}]...", tenantId);

        List<Account> accounts = createAccounts(tenantId, primaryOwner, secondaryOwner);
        List<Transaction> transactions = createTransactions(tenantId, accounts);

        log.info("  Saved {} accounts, {} transactions for [{}]",
                accounts.size(), transactions.size(), tenantId);

        log.info("  Vector ingestion complete for [{}]", tenantId);
    }

    // ── Account creation ──────────────────────────────────────────────────
    private List<Account> createAccounts(String tenantId, String primary, String secondary) {
        return accountRepository.saveAll(List.of(

                Account.builder()
                        .tenantId(tenantId)
                        .ownerName(primary)
                        .accountType("CURRENT")
                        .currency("PLN")
                        .balance(new BigDecimal("8432.50"))
                        .build(),

                Account.builder()
                        .tenantId(tenantId)
                        .ownerName(primary)
                        .accountType("SAVINGS")
                        .currency("PLN")
                        .balance(new BigDecimal("42000.00"))
                        .build(),

                Account.builder()
                        .tenantId(tenantId)
                        .ownerName(primary)
                        .accountType("BUSINESS")
                        .currency("PLN")
                        .balance(new BigDecimal("125800.00"))
                        .build(),

                Account.builder()
                        .tenantId(tenantId)
                        .ownerName(secondary)
                        .accountType("CURRENT")
                        .currency("EUR")
                        .balance(new BigDecimal("3200.00"))
                        .build()
        ));
    }

    // ── Transaction creation ──────────────────────────────────────────────
    private List<Transaction> createTransactions(String tenantId, List<Account> accounts) {

        // Resolve accounts by type for readability
        Account current = byType(accounts, "CURRENT");
        Account savings = byType(accounts, "SAVINGS");
        Account business = byType(accounts, "BUSINESS");

        List<Transaction> all = new ArrayList<>();

        // ── January ──────────────────────────────────────────────────────
        all.addAll(List.of(
                debit(tenantId, current.getId(), "485.20", "PLN", "FOOD", LocalDate.of(2026, 1, 3), "Carrefour", "Weekly grocery shopping at Carrefour, household supplies"),
                credit(tenantId, current.getId(), "15000.00", "PLN", "SALARY", LocalDate.of(2026, 1, 5), "ACME Corp", "January salary from ACME Corp"),
                debit(tenantId, current.getId(), "120.00", "PLN", "UTILITIES", LocalDate.of(2026, 1, 7), "PGNiG", "Natural gas bill for January"),
                debit(tenantId, current.getId(), "210.00", "PLN", "UTILITIES", LocalDate.of(2026, 1, 8), "Tauron", "Electricity bill for flat January"),
                debit(tenantId, current.getId(), "55.00", "PLN", "SUBSCRIPTION", LocalDate.of(2026, 1, 10), "Netflix", "Netflix monthly subscription"),
                debit(tenantId, current.getId(), "29.99", "PLN", "SUBSCRIPTION", LocalDate.of(2026, 1, 10), "Spotify", "Spotify Premium monthly subscription"),
                debit(tenantId, current.getId(), "1200.00", "PLN", "HOUSING", LocalDate.of(2026, 1, 12), "Landlord W. Zając", "Monthly rent for flat in Mokotów district"),
                debit(tenantId, current.getId(), "340.00", "PLN", "FOOD", LocalDate.of(2026, 1, 15), "Sushi Kyo", "Business lunch with client, Japanese restaurant"),
                debit(tenantId, current.getId(), "89.00", "PLN", "HEALTH", LocalDate.of(2026, 1, 17), "Medicover", "Monthly Medicover private health insurance premium"),
                debit(tenantId, current.getId(), "250.00", "PLN", "TRAVEL", LocalDate.of(2026, 1, 20), "PKP Intercity", "Train tickets Warsaw to Gdańsk and return, weekend trip"),
                debit(tenantId, current.getId(), "74.50", "PLN", "FUEL", LocalDate.of(2026, 1, 22), "Orlen", "Fuel top-up at Orlen station"),
                debit(tenantId, savings.getId(), "2000.00", "PLN", "SAVINGS", LocalDate.of(2026, 1, 25), "Self transfer", "Monthly transfer to savings account"),
                credit(tenantId, current.getId(), "800.00", "PLN", "FREELANCE", LocalDate.of(2026, 1, 28), "K. Wiśniewski", "Freelance payment for logo design project"),
                debit(tenantId, current.getId(), "120.00", "PLN", "FOOD", LocalDate.of(2026, 1, 30), "Żabka", "Convenience store purchases throughout January")
        ));

        // ── February ─────────────────────────────────────────────────────
        all.addAll(List.of(
                credit(tenantId, current.getId(), "15000.00", "PLN", "SALARY", LocalDate.of(2026, 2, 5), "ACME Corp", "February salary from ACME Corp"),
                debit(tenantId, current.getId(), "430.00", "PLN", "FOOD", LocalDate.of(2026, 2, 6), "Biedronka", "Grocery shopping at Biedronka, weekly run"),
                debit(tenantId, current.getId(), "115.00", "PLN", "UTILITIES", LocalDate.of(2026, 2, 7), "PGNiG", "Gas bill for February, increased due to cold weather"),
                debit(tenantId, current.getId(), "1200.00", "PLN", "HOUSING", LocalDate.of(2026, 2, 12), "Landlord W. Zając", "Monthly rent for flat in Mokotów district"),
                debit(tenantId, current.getId(), "199.00", "PLN", "SHOPPING", LocalDate.of(2026, 2, 14), "Empik", "Books and Valentine's Day gift from Empik"),
                debit(tenantId, current.getId(), "560.00", "PLN", "TRAVEL", LocalDate.of(2026, 2, 16), "Ryanair", "Flight tickets Warsaw to Barcelona for spring break"),
                debit(tenantId, current.getId(), "89.00", "PLN", "HEALTH", LocalDate.of(2026, 2, 17), "Medicover", "Monthly health insurance premium Medicover"),
                debit(tenantId, current.getId(), "55.00", "PLN", "SUBSCRIPTION", LocalDate.of(2026, 2, 10), "Netflix", "Netflix monthly subscription"),
                debit(tenantId, current.getId(), "270.00", "PLN", "FOOD", LocalDate.of(2026, 2, 20), "Restauracja Polska", "Team dinner at traditional Polish restaurant"),
                credit(tenantId, current.getId(), "500.00", "PLN", "FREELANCE", LocalDate.of(2026, 2, 22), "M. Jabłoński", "Partial payment for website development project"),
                debit(tenantId, current.getId(), "320.00", "PLN", "CLOTHING", LocalDate.of(2026, 2, 24), "Zara", "Winter clothing sale, jacket and trousers"),
                debit(tenantId, savings.getId(), "2000.00", "PLN", "SAVINGS", LocalDate.of(2026, 2, 25), "Self transfer", "Monthly savings transfer"),
                debit(tenantId, current.getId(), "48.00", "PLN", "FUEL", LocalDate.of(2026, 2, 27), "BP", "Fuel at BP station, city driving")
        ));

        // ── March ─────────────────────────────────────────────────────────
        all.addAll(List.of(
                debit(tenantId, current.getId(), "230.50", "PLN", "FOOD", LocalDate.of(2026, 3, 1), "Biedronka", "Grocery shopping at Biedronka, weekly supplies"),
                debit(tenantId, current.getId(), "89.99", "PLN", "TRAVEL", LocalDate.of(2026, 3, 3), "Uber", "Uber ride to Warsaw airport, business trip to Berlin"),
                credit(tenantId, current.getId(), "15000.00", "PLN", "SALARY", LocalDate.of(2026, 3, 5), "ACME Corp", "March salary from ACME Corp"),
                debit(tenantId, current.getId(), "1200.00", "PLN", "HOUSING", LocalDate.of(2026, 3, 10), "Landlord W. Zając", "March rent for flat in Mokotów district"),
                debit(tenantId, current.getId(), "450.00", "PLN", "FOOD", LocalDate.of(2026, 3, 14), "Sushi Waka", "Client entertainment dinner at Sushi Waka"),
                debit(tenantId, current.getId(), "75.00", "PLN", "SUBSCRIPTION", LocalDate.of(2026, 3, 15), "Netflix/Spotify", "Netflix and Spotify monthly subscriptions combined"),
                debit(tenantId, current.getId(), "2300.00", "PLN", "SHOPPING", LocalDate.of(2026, 3, 18), "MediaMarkt", "New laptop Dell XPS 15 for home office setup"),
                debit(tenantId, current.getId(), "180.00", "PLN", "HEALTH", LocalDate.of(2026, 3, 20), "Medicover", "Private specialist visit and prescribed medications"),
                credit(tenantId, current.getId(), "500.00", "PLN", "FREELANCE", LocalDate.of(2026, 3, 22), "M. Jabłoński", "Final payment for website development project"),
                debit(tenantId, current.getId(), "340.00", "PLN", "TRAVEL", LocalDate.of(2026, 3, 25), "PKP Intercity", "Return train tickets Warsaw to Kraków weekend trip"),
                debit(tenantId, savings.getId(), "5000.00", "PLN", "INVESTMENT", LocalDate.of(2026, 3, 28), "mBank Investment", "Quarterly top-up of ETF investment portfolio"),
                debit(tenantId, current.getId(), "89.00", "PLN", "HEALTH", LocalDate.of(2026, 3, 17), "Medicover", "Monthly Medicover premium"),
                debit(tenantId, current.getId(), "165.00", "PLN", "UTILITIES", LocalDate.of(2026, 3, 8), "Tauron", "Electricity bill March")
        ));

        // ── Business account transactions ─────────────────────────────────
        all.addAll(List.of(
                credit(tenantId, business.getId(), "45000.00", "PLN", "REVENUE", LocalDate.of(2026, 1, 15), "Client Alpha Sp. z o.o.", "Invoice #2026/01/001 — software development services Q4 2023"),
                debit(tenantId, business.getId(), "8500.00", "PLN", "PAYROLL", LocalDate.of(2026, 1, 31), "Employee Payroll", "January payroll for 3 contractors"),
                debit(tenantId, business.getId(), "2200.00", "PLN", "OFFICE", LocalDate.of(2026, 1, 10), "WeWork", "Coworking space monthly membership Warsaw"),
                debit(tenantId, business.getId(), "490.00", "PLN", "SOFTWARE", LocalDate.of(2026, 1, 5), "JetBrains", "Annual JetBrains All Products Pack license renewal"),
                debit(tenantId, business.getId(), "320.00", "PLN", "SOFTWARE", LocalDate.of(2026, 1, 5), "GitHub", "GitHub Teams annual subscription"),
                credit(tenantId, business.getId(), "32000.00", "PLN", "REVENUE", LocalDate.of(2026, 2, 20), "Client Beta S.A.", "Invoice #2026/02/001 — UX audit and design consulting"),
                debit(tenantId, business.getId(), "8500.00", "PLN", "PAYROLL", LocalDate.of(2026, 2, 28), "Employee Payroll", "February payroll for contractors"),
                debit(tenantId, business.getId(), "1800.00", "PLN", "MARKETING", LocalDate.of(2026, 2, 12), "Google Ads", "Google Ads campaign budget February"),
                credit(tenantId, business.getId(), "61500.00", "PLN", "REVENUE", LocalDate.of(2026, 3, 10), "Client Gamma GmbH", "Invoice #2026/03/001 — full-stack development sprint 1 and 2"),
                debit(tenantId, business.getId(), "8500.00", "PLN", "PAYROLL", LocalDate.of(2026, 3, 31), "Employee Payroll", "March payroll for contractors"),
                debit(tenantId, business.getId(), "3400.00", "PLN", "TRAVEL", LocalDate.of(2026, 3, 4), "Lufthansa", "Business flights Berlin for client meeting, 3 team members"),
                debit(tenantId, business.getId(), "950.00", "PLN", "OFFICE", LocalDate.of(2026, 3, 15), "Office Depot", "Office supplies and equipment Q1")
        ));

        return transactionRepository.saveAll(all);
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private Transaction debit(String tenantId, String accountId, String amount, String currency,
                              String category, LocalDate date, String counterparty, String description) {
        return Transaction.builder()
                .tenantId(tenantId)
                .accountId(accountId)
                .amount(new BigDecimal(amount))
                .currency(currency)
                .type("DEBIT")
                .category(category)
                .date(date)
                .counterparty(counterparty)
                .description(description)
                .build();
    }

    private Transaction credit(String tenantId, String accountId, String amount, String currency,
                               String category, LocalDate date, String counterparty, String description) {
        return Transaction.builder()
                .tenantId(tenantId)
                .accountId(accountId)
                .amount(new BigDecimal(amount))
                .currency(currency)
                .type("CREDIT")
                .category(category)
                .date(date)
                .counterparty(counterparty)
                .description(description)
                .build();
    }

    private Account byType(List<Account> accounts, String type) {
        return accounts.stream()
                .filter(a -> a.getAccountType().equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No account of type: " + type));
    }
}