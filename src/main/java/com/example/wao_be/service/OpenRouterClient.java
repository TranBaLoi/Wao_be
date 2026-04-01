package com.example.wao_be.service;

import com.example.wao_be.config.OpenRouterProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenRouterClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenRouterClient.class);

    private final OpenRouterProperties properties;

    public ChatCompletionResult chat(String model,
                                     List<ChatRequestMessage> messages,
                                     double temperature,
                                     int maxTokens) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException("Missing OPENROUTER API key. Configure openrouter.api-key.");
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                    .build();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("stream", false);

            String payload = OBJECT_MAPPER.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.getBaseUrl()))
                    .timeout(Duration.ofMillis(properties.getReadTimeoutMs()))
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .header("HTTP-Referer", properties.getSiteUrl())
                    .header("X-Title", properties.getAppName())
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < HttpStatus.OK.value() || response.statusCode() >= HttpStatus.MULTIPLE_CHOICES.value()) {
                throw new IllegalStateException("OpenRouter error " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = OBJECT_MAPPER.readTree(response.body());
            JsonNode errorNode = root.path("error");
            if (errorNode != null && !errorNode.isMissingNode() && !errorNode.isNull()) {
                throw new IllegalStateException("OpenRouter error payload: " + errorNode.toString());
            }
            String content = extractContent(root);
            if (content.isBlank()) {
                LOGGER.warn("OpenRouter returned empty content for model {}. Raw response: {}", model, response.body());
            }

            JsonNode usage = root.path("usage");
            return ChatCompletionResult.builder()
                    .content(content)
                    .promptTokens(toNullableInt(usage.path("prompt_tokens")))
                    .completionTokens(toNullableInt(usage.path("completion_tokens")))
                    .totalTokens(toNullableInt(usage.path("total_tokens")))
                    .build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to call OpenRouter: " + ex.getMessage(), ex);
        }
    }

    private String extractContent(JsonNode root) {
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return "";
        }

        JsonNode firstChoice = choices.get(0);
        String content = coerceContentToText(firstChoice.path("message").path("content"));
        if (!content.isBlank()) {
            return content;
        }

        content = coerceContentToText(firstChoice.path("text"));
        if (!content.isBlank()) {
            return content;
        }

        return coerceContentToText(firstChoice.path("output_text"));
    }

    private String coerceContentToText(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }

        if (node.isTextual()) {
            return node.asText("");
        }

        if (node.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : node) {
                String piece = "";
                if (item.isTextual()) {
                    piece = item.asText("");
                } else if (item.isObject()) {
                    piece = item.path("text").asText("");
                    if (piece.isBlank()) {
                        piece = item.path("content").asText("");
                    }
                }

                if (!piece.isBlank()) {
                    if (!builder.isEmpty()) {
                        builder.append(" ");
                    }
                    builder.append(piece);
                }
            }
            return builder.toString();
        }

        if (node.isObject()) {
            String text = node.path("text").asText("");
            if (!text.isBlank()) {
                return text;
            }
            return node.path("content").asText("");
        }

        return "";
    }

    private Integer toNullableInt(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull() ? null : node.asInt();
    }

    @Data
    @Builder
    public static class ChatCompletionResult {
        private String content;
        private Integer promptTokens;
        private Integer completionTokens;
        private Integer totalTokens;
    }

    @Data
    @Builder
    public static class ChatRequestMessage {
        private String role;
        private String content;

        public static List<ChatRequestMessage> withSystemPrompt(String systemPrompt,
                                                                List<ChatRequestMessage> history) {
            List<ChatRequestMessage> finalMessages = new ArrayList<>();
            finalMessages.add(ChatRequestMessage.builder().role("system").content(systemPrompt).build());
            finalMessages.addAll(history);
            return finalMessages;
        }
    }
}
