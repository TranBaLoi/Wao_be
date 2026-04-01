package com.example.wao_be.dto;

import com.example.wao_be.entity.ChatMessage;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class ChatbotDto {

    @Data
    public static class SendMessageRequest {
        private Long conversationId;

        @NotBlank
        private String message;
    }

    @Data
    public static class SendMessageResponse {
        private Long conversationId;
        private Long assistantMessageId;
        private String answer;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ConversationSummary {
        private Long id;
        private String title;
        private String model;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class MessageItem {
        private Long id;
        private ChatMessage.Role role;
        private String content;
        private Integer totalTokens;
        private LocalDateTime createdAt;
    }

    @Data
    public static class ConversationDetail {
        private Long conversationId;
        private String title;
        private String model;
        private List<MessageItem> messages;
    }
}
