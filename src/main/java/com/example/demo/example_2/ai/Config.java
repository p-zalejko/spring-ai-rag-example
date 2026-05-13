package com.example.demo.example_2.ai;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    // register the @tools within MCP server
    @Bean
    public ToolCallbackProvider tools(FinancialTools financialTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(financialTools)
                .build();
    }
}
