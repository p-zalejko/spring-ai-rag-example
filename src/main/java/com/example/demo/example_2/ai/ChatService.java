package com.example.demo.example_2.ai;

import com.example.demo.example_2.domain.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient.Builder chatClientBuilder;
    private final FinancialTools financialTools;
    // In a real app: store per-user session in Redis or MongoDB
    private final List<Message> conversationHistory = new ArrayList<>();

    private static final String SYSTEM_PROMPT = """
            You are a helpful financial assistant for a multitenant platform.
            You have access to tools that can query real financial data (accounts, transactions, spending).
            You also receive relevant context from previous transactions as semantic search results.
            
            Guidelines:
            - Always be precise with monetary amounts.
            - Never guess financial data — use the provided tools to fetch real information.
            - If you're unsure, ask the user to clarify which account or time period they mean.
            - Format currency amounts clearly (e.g. 1,234.56 PLN).
            - Keep answers concise but complete.
            """;

    public String chat(String userMessage) {
        String tenantId = TenantContext.current();
        log.debug("Chat request: tenant={}, message={}", tenantId, userMessage);

        // ── Step 3: Call Claude with tools registered ─────────────────────
        // Spring AI handles the tool-call loop:
        //   1. AI receives tools + message
        //   2. AI may call a tool → Spring AI executes the Java method
        //   3. Result fed back to AI → AI generates final answer
        //   (repeats until AI produces a text response)
        String response = chatClientBuilder.build()
                .prompt()
                .system(SYSTEM_PROMPT)
                .messages(conversationHistory)
                .user(userMessage)
                .tools(financialTools)       // register all @Tool methods
                .call()
                .content();

        // ── Step 4: Update conversation history ───────────────────────────
        conversationHistory.add(new UserMessage(userMessage)); // store original (not enriched)
        conversationHistory.add(new AssistantMessage(response));

        // Keep last 20 messages to avoid context window overflow
        if (conversationHistory.size() > 20) {
            conversationHistory.subList(0, conversationHistory.size() - 20).clear();
        }

        return response;
    }

    public void clearHistory() {
        conversationHistory.clear();
    }
}