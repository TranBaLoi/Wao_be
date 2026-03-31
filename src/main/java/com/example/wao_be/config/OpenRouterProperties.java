package com.example.wao_be.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Component
@ConfigurationProperties(prefix = "openrouter")
public class OpenRouterProperties {
    private String baseUrl = "https://openrouter.ai/api/v1/chat/completions";
    private String apiKey;
    private String model = "openai/gpt-4o-mini";
    private String siteUrl = "http://localhost";
    private String appName = "Wao Mobile";
    private int connectTimeoutMs = 10000;
    private int readTimeoutMs = 60000;
    private int maxTokens = 500;
    private double temperature = 0.7;
    private int maxHistoryMessages = 20;
}
