package com.example.wao_be.service;

import com.example.wao_be.config.OpenRouterProperties;
import com.example.wao_be.dto.ChatbotDto;
import com.example.wao_be.entity.ChatConversation;
import com.example.wao_be.entity.ChatMessage;
import com.example.wao_be.entity.Food;
import com.example.wao_be.entity.MealPlan;
import com.example.wao_be.entity.User;
import com.example.wao_be.entity.UserFoodLog;
import com.example.wao_be.repository.ChatConversationRepository;
import com.example.wao_be.repository.ChatMessageRepository;
import com.example.wao_be.repository.FoodRepository;
import com.example.wao_be.repository.MealPlanRepository;
import com.example.wao_be.repository.UserFoodLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatbotService {

    private static final String DEFAULT_SYSTEM_PROMPT = "You are the AI assistant for the Wao nutrition tracking app. " +
            "You can answer topics related to diet, calories, meal planning, food logging, hydration, healthy habits, and basic cooking/recipes. " +
            "If the topic is unrelated to food or cooking, refuse and say you can only help with nutrition and cooking questions. " +
            "When helpful, suggest using app features like food log, meal plan, daily summary, or water log. " +
            "Answer in Vietnamese only, clearly and briefly in plain text. " +
            "Avoid markdown, bullet lists, and long explanations. " +
            "If the question is short, reply in 1-3 sentences. " +
            "Do not provide definitive medical diagnosis and advise professional care for serious issues.";

    private static final int MAX_SHORT_ANSWER_CHARS = 600;
    private static final int MAX_LONG_ANSWER_CHARS = 1200;
    private static final int MAX_DATA_CONTEXT_CHARS = 1200;
    private static final int MAX_RECENT_LOGS = 8;
    private static final int MAX_MEAL_PLANS = 2;
    private static final int MAX_FOOD_SAMPLES = 6;

    private final UserService userService;
    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final OpenRouterClient openRouterClient;
    private final OpenRouterProperties openRouterProperties;
    private final UserFoodLogRepository userFoodLogRepository;
    private final MealPlanRepository mealPlanRepository;
    private final FoodRepository foodRepository;

    public ChatbotDto.SendMessageResponse sendMessage(Long userId, ChatbotDto.SendMessageRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Message must not be empty");
        }

        ChatConversation conversation = resolveConversation(userId, request);

        ChatMessage userMessage = ChatMessage.builder()
                .conversation(conversation)
                .role(ChatMessage.Role.USER)
                .content(request.getMessage().trim())
                .build();
        messageRepository.save(userMessage);

        List<OpenRouterClient.ChatRequestMessage> contextMessages = buildContextMessages(conversation.getId(), userId);
        String model = conversation.getModel() != null ? conversation.getModel() : openRouterProperties.getModel();
        double temperature = openRouterProperties.getTemperature();
        int maxTokens = openRouterProperties.getMaxTokens();

        OpenRouterClient.ChatCompletionResult result = openRouterClient.chat(
                model,
                OpenRouterClient.ChatRequestMessage.withSystemPrompt(DEFAULT_SYSTEM_PROMPT, contextMessages),
                temperature,
                maxTokens
        );
        String normalizedAnswer = normalizeAnswer(result.getContent(), request.getMessage());

        ChatMessage assistantMessage = ChatMessage.builder()
                .conversation(conversation)
                .role(ChatMessage.Role.ASSISTANT)
                .content(normalizedAnswer)
                .promptTokens(result.getPromptTokens())
                .completionTokens(result.getCompletionTokens())
                .totalTokens(result.getTotalTokens())
                .build();

        ChatMessage savedAssistant = messageRepository.save(assistantMessage);
        conversation.setModel(model);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        ChatbotDto.SendMessageResponse response = new ChatbotDto.SendMessageResponse();
        response.setConversationId(conversation.getId());
        response.setAssistantMessageId(savedAssistant.getId());
        response.setAnswer(normalizedAnswer);
        response.setCreatedAt(savedAssistant.getCreatedAt());
        return response;
    }

    private String normalizeAnswer(String answer, String userMessage) {
        if (answer == null) {
            return "";
        }

        String normalized = answer.replace("\r", "\n").replace("\n", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();

        int limit = userMessage != null && userMessage.trim().length() <= 60
                ? MAX_SHORT_ANSWER_CHARS
                : MAX_LONG_ANSWER_CHARS;
        if (normalized.length() > limit) {
            normalized = normalized.substring(0, limit).trim();
        }

        return normalized;
    }

    @Transactional(readOnly = true)
    public List<ChatbotDto.ConversationSummary> getConversations(Long userId) {
        userService.findById(userId);
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatbotDto.ConversationDetail getConversationDetail(Long userId, Long conversationId) {
        ChatConversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found: " + conversationId));

        List<ChatMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());

        ChatbotDto.ConversationDetail detail = new ChatbotDto.ConversationDetail();
        detail.setConversationId(conversation.getId());
        detail.setTitle(conversation.getTitle());
        detail.setModel(conversation.getModel());
        detail.setMessages(messages.stream().map(this::toMessageItem).toList());
        return detail;
    }

    public void deleteConversation(Long userId, Long conversationId) {
        ChatConversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found: " + conversationId));
        conversationRepository.delete(conversation);
    }

    private ChatConversation resolveConversation(Long userId, ChatbotDto.SendMessageRequest request) {
        if (request.getConversationId() != null) {
            return conversationRepository.findByIdAndUserId(request.getConversationId(), userId)
                    .orElseThrow(() -> new EntityNotFoundException("Conversation not found: " + request.getConversationId()));
        }

        User user = userService.findById(userId);
        String title = request.getMessage().trim();
        if (title.length() > 50) {
            title = title.substring(0, 50) + "...";
        }

        return conversationRepository.save(ChatConversation.builder()
                .user(user)
                .title(title)
                .model(openRouterProperties.getModel())
                .build());
    }

    private List<OpenRouterClient.ChatRequestMessage> buildContextMessages(Long conversationId, Long userId) {
        List<ChatMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        if (messages.isEmpty()) {
            return Collections.emptyList();
        }

        int limit = Math.max(openRouterProperties.getMaxHistoryMessages(), 2);
        int start = Math.max(messages.size() - limit, 0);
        List<ChatMessage> recentMessages = new ArrayList<>(messages.subList(start, messages.size()));

        List<OpenRouterClient.ChatRequestMessage> history = recentMessages.stream()
                .filter(m -> m.getRole() == ChatMessage.Role.USER || m.getRole() == ChatMessage.Role.ASSISTANT)
                .map(m -> OpenRouterClient.ChatRequestMessage.builder()
                        .role(m.getRole().name().toLowerCase())
                        .content(m.getContent())
                        .build())
                .toList();

        String dataContext = buildUserDataContext(userId);
        if (!dataContext.isBlank()) {
            List<OpenRouterClient.ChatRequestMessage> withContext = new ArrayList<>();
            withContext.add(OpenRouterClient.ChatRequestMessage.builder()
                    .role("system")
                    .content(dataContext)
                    .build());
            withContext.addAll(history);
            return withContext;
        }

        return history;
    }

    private String buildUserDataContext(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(6);

        Double todayCalories = userFoodLogRepository.sumCaloriesByUserIdAndLogDate(userId, today);
        List<UserFoodLog> recentLogs = userFoodLogRepository.findByUserIdAndLogDateBetween(userId, fromDate, today);
        recentLogs.sort(Comparator.comparing(UserFoodLog::getLogDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        List<MealPlan> userPlans = mealPlanRepository.findByTypeAndUserId(MealPlan.MealPlanType.USER_CUSTOM, userId);
        userPlans.sort(Comparator.comparing(MealPlan::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());

        List<Food> verifiedFoods = foodRepository.findByIsVerified(true);

        StringBuilder builder = new StringBuilder();
        builder.append("Du lieu nguoi dung (tom tat tu CSDL, chi dung cho chatbot). ");

        if (todayCalories != null && todayCalories > 0) {
            builder.append("Tong calo hom nay khoang ")
                    .append(Math.round(todayCalories))
                    .append(" kcal. ");
        }

        if (!recentLogs.isEmpty()) {
            builder.append("Nhat ky an uong 7 ngay gan day: ");
            int count = 0;
            for (UserFoodLog log : recentLogs) {
                if (count >= MAX_RECENT_LOGS) {
                    break;
                }
                String foodName = log.getFood() != null ? log.getFood().getName() : "";
                builder.append(log.getLogDate())
                        .append(" ")
                        .append(log.getMealType() != null ? log.getMealType().name() : "")
                        .append(" ")
                        .append(foodName)
                        .append(" x")
                        .append(log.getServingQty() != null ? log.getServingQty() : 0)
                        .append(" (~")
                        .append(log.getTotalCalories() != null ? Math.round(log.getTotalCalories()) : 0)
                        .append(" kcal); ");
                count++;
            }
        } else {
            builder.append("Chua co nhat ky an uong gan day. ");
        }

        if (!userPlans.isEmpty()) {
            builder.append("Meal plan cua ban: ");
            int count = 0;
            for (MealPlan plan : userPlans) {
                if (count >= MAX_MEAL_PLANS) {
                    break;
                }
                builder.append(plan.getName())
                        .append(" (")
                        .append(plan.getTotalCalories() != null ? Math.round(plan.getTotalCalories()) : 0)
                        .append(" kcal)");

                if (plan.getMealPlanFoods() != null && !plan.getMealPlanFoods().isEmpty()) {
                    builder.append(" gom ");
                    int foodCount = 0;
                    for (var item : plan.getMealPlanFoods()) {
                        if (foodCount >= 3) {
                            break;
                        }
                        String foodName = item.getFood() != null ? item.getFood().getName() : "";
                        builder.append(foodName)
                                .append(" x")
                                .append(item.getServingQty() != null ? item.getServingQty() : 0)
                                .append(", ");
                        foodCount++;
                    }
                }
                builder.append("; ");
                count++;
            }
        } else {
            builder.append("Chua co meal plan tu tao. ");
        }

        if (!verifiedFoods.isEmpty()) {
            builder.append("Mon an mau (danh muc san co): ");
            int count = 0;
            for (Food food : verifiedFoods) {
                if (count >= MAX_FOOD_SAMPLES) {
                    break;
                }
                builder.append(food.getName())
                        .append(" (")
                        .append(food.getCalories() != null ? Math.round(food.getCalories()) : 0)
                        .append(" kcal), ");
                count++;
            }
        }

        if (builder.length() > MAX_DATA_CONTEXT_CHARS) {
            return builder.substring(0, MAX_DATA_CONTEXT_CHARS).trim();
        }
        return builder.toString().trim();
    }

    private ChatbotDto.ConversationSummary toSummary(ChatConversation c) {
        ChatbotDto.ConversationSummary summary = new ChatbotDto.ConversationSummary();
        summary.setId(c.getId());
        summary.setTitle(c.getTitle());
        summary.setModel(c.getModel());
        summary.setCreatedAt(c.getCreatedAt());
        summary.setUpdatedAt(c.getUpdatedAt());
        return summary;
    }

    private ChatbotDto.MessageItem toMessageItem(ChatMessage m) {
        ChatbotDto.MessageItem item = new ChatbotDto.MessageItem();
        item.setId(m.getId());
        item.setRole(m.getRole());
        item.setContent(m.getContent());
        item.setTotalTokens(m.getTotalTokens());
        item.setCreatedAt(m.getCreatedAt());
        return item;
    }
}
