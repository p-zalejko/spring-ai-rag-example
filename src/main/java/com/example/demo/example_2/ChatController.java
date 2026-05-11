package com.example.demo.example_2;

import com.example.demo.example_2.ai.ChatService;
import com.example.demo.example_2.domain.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/*

# Ask about balances
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-acme" \
  -d '{"message": "What is my total balance?"}'

# Ask a semantic question (hits vector search)
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-acme" \
  -d '{"message": "Find my Berlin business trip payment"}'

# Ask for spending analysis (hits tool calling)
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: tenant-acme" \
  -d '{"message": "Break down my March 2024 spending by category"}'

# Clear conversation history
curl -X DELETE http://localhost:8080/api/chat/history

 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
class ChatController {

    private final ChatService chatService;

    /**
     * POST /api/chat
     * Header: X-Tenant-Id: tenant-abc
     * Body: { "message": "What is my balance?" }
     */
    @PostMapping
    ChatResponse chat(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestBody ChatRequest request) {
        try {
            TenantContext.set(tenantId);
            String response = chatService.chat(request.message());
            return new ChatResponse(response);
        } finally {
            TenantContext.clear();   // always clean up ThreadLocal
        }
    }

    @DeleteMapping("/history")
    void clearHistory() {
        chatService.clearHistory();
    }

    record ChatRequest(String message) {
    }

    record ChatResponse(String answer) {
    }
}