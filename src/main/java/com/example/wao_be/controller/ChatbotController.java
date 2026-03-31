package com.example.wao_be.controller;
import com.example.wao_be.dto.ChatbotDto;
import com.example.wao_be.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/users/{userId}/chat")
@RequiredArgsConstructor
public class ChatbotController {
    private final ChatbotService chatbotService;
    @GetMapping("/conversations")
    public ResponseEntity<List<ChatbotDto.ConversationSummary>> getConversations(@PathVariable Long userId) {
        return ResponseEntity.ok(chatbotService.getConversations(userId));
    }
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ChatbotDto.ConversationDetail> getConversationDetail(
            @PathVariable Long userId,
            @PathVariable Long conversationId) {
        return ResponseEntity.ok(chatbotService.getConversationDetail(userId, conversationId));
    }
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable Long userId,
            @PathVariable Long conversationId) {
        chatbotService.deleteConversation(userId, conversationId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/messages")
    public ResponseEntity<ChatbotDto.SendMessageResponse> sendMessage(
            @PathVariable Long userId,
            @Valid @RequestBody ChatbotDto.SendMessageRequest request) {
        return ResponseEntity.ok(chatbotService.sendMessage(userId, request));
    }
}
