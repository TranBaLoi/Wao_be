// phan cua nam
package com.example.wao_be.config;

import com.example.wao_be.dto.HealthProfileDto;
import com.example.wao_be.entity.Food;
import com.example.wao_be.entity.StepLog;
import com.example.wao_be.entity.User;
import com.example.wao_be.entity.UserFoodLog;
import com.example.wao_be.entity.UserWaterLog;
import com.example.wao_be.repository.FoodRepository;
import com.example.wao_be.repository.StepLogRepository;
import com.example.wao_be.repository.UserFoodLogRepository;
import com.example.wao_be.repository.UserRepository;
import com.example.wao_be.repository.UserWaterLogRepository;
import com.example.wao_be.service.DailySummaryService;
import com.example.wao_be.service.HealthProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DemoDataSeeder {

    private static final String DEMO_EMAIL = "nam.demo@wao.local";

    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final UserFoodLogRepository userFoodLogRepository;
    private final UserWaterLogRepository userWaterLogRepository;
    private final StepLogRepository stepLogRepository;
    private final HealthProfileService healthProfileService;
    private final DailySummaryService dailySummaryService;
    private final JdbcTemplate jdbcTemplate;

    @Bean
    CommandLineRunner seedDemoData() {
        return args -> {
            if (userRepository.existsByEmail(DEMO_EMAIL)) {
                return;
            }

            User user = userRepository.save(User.builder()
                    .email(DEMO_EMAIL)
                    .passwordHash("123456")
                    .fullName("Nam Demo")
                    .status(User.UserStatus.ACTIVE)
                    .img("https://example.com/demo-user.png")
                    .build());

            seedHealthProfiles(user.getId());
            seedWeightLogs(user.getId());

            Map<String, Food> foods = seedFoods();
            seedFoodLogs(user, foods);
            seedWaterLogs(user);
            seedStepLogs(user);
            refreshDailySummaries(user.getId());
        };
    }

    private void seedHealthProfiles(Long userId) {
        HealthProfileDto.Request first = new HealthProfileDto.Request();
        first.setGender(com.example.wao_be.entity.UserHealthProfile.Gender.MALE);
        first.setDob(LocalDate.of(2000, 5, 20));
        first.setHeightCm(172.0);
        first.setWeightKg(78.5);
        first.setActivityLevel(com.example.wao_be.entity.UserHealthProfile.ActivityLevel.LIGHTLY_ACTIVE);
        first.setGoalType(com.example.wao_be.entity.UserHealthProfile.GoalType.LOSE_WEIGHT);
        healthProfileService.create(userId, first);

        HealthProfileDto.Request second = new HealthProfileDto.Request();
        second.setGender(com.example.wao_be.entity.UserHealthProfile.Gender.MALE);
        second.setDob(LocalDate.of(2000, 5, 20));
        second.setHeightCm(172.0);
        second.setWeightKg(77.8);
        second.setActivityLevel(com.example.wao_be.entity.UserHealthProfile.ActivityLevel.MODERATELY_ACTIVE);
        second.setGoalType(com.example.wao_be.entity.UserHealthProfile.GoalType.LOSE_WEIGHT);
        healthProfileService.create(userId, second);

        HealthProfileDto.Request third = new HealthProfileDto.Request();
        third.setGender(com.example.wao_be.entity.UserHealthProfile.Gender.MALE);
        third.setDob(LocalDate.of(2000, 5, 20));
        third.setHeightCm(172.0);
        third.setWeightKg(76.9);
        third.setActivityLevel(com.example.wao_be.entity.UserHealthProfile.ActivityLevel.MODERATELY_ACTIVE);
        third.setGoalType(com.example.wao_be.entity.UserHealthProfile.GoalType.LOSE_WEIGHT);
        healthProfileService.create(userId, third);
    }

    private Map<String, Food> seedFoods() {
        Map<String, Food> foods = new LinkedHashMap<>();
        foods.put("Oatmeal Bowl", saveFood("Oatmeal Bowl", "1 bowl", 320.0, 12.0, 54.0, 6.0));
        foods.put("Chicken Breast", saveFood("Chicken Breast", "150g", 248.0, 46.5, 0.0, 5.4));
        foods.put("Brown Rice", saveFood("Brown Rice", "1 bowl", 216.0, 5.0, 44.8, 1.8));
        foods.put("Banana", saveFood("Banana", "1 fruit", 105.0, 1.3, 27.0, 0.4));
        foods.put("Greek Yogurt", saveFood("Greek Yogurt", "1 cup", 130.0, 11.0, 9.0, 4.0));
        foods.put("Salmon Salad", saveFood("Salmon Salad", "1 plate", 410.0, 30.0, 18.0, 24.0));
        foods.put("Egg Sandwich", saveFood("Egg Sandwich", "1 sandwich", 290.0, 14.0, 28.0, 12.0));
        return foods;
    }

    private Food saveFood(String name, String servingSize, Double calories, Double protein, Double carbs, Double fat) {
        return foodRepository.save(Food.builder()
                .name(name)
                .servingSize(servingSize)
                .calories(calories)
                .protein(protein)
                .carbs(carbs)
                .fat(fat)
                .isVerified(true)
                .build());
    }

    private void seedFoodLogs(User user, Map<String, Food> foods) {
        LocalDate today = LocalDate.now();

        saveFoodLog(user, foods.get("Oatmeal Bowl"), UserFoodLog.MealType.BREAKFAST, 1.0, today.minusDays(6));
        saveFoodLog(user, foods.get("Banana"), UserFoodLog.MealType.SNACK, 1.0, today.minusDays(6));
        saveFoodLog(user, foods.get("Chicken Breast"), UserFoodLog.MealType.LUNCH, 1.0, today.minusDays(6));
        saveFoodLog(user, foods.get("Brown Rice"), UserFoodLog.MealType.LUNCH, 1.0, today.minusDays(6));
        saveFoodLog(user, foods.get("Salmon Salad"), UserFoodLog.MealType.DINNER, 1.0, today.minusDays(6));

        saveFoodLog(user, foods.get("Egg Sandwich"), UserFoodLog.MealType.BREAKFAST, 1.0, today.minusDays(5));
        saveFoodLog(user, foods.get("Greek Yogurt"), UserFoodLog.MealType.SNACK, 1.0, today.minusDays(5));
        saveFoodLog(user, foods.get("Chicken Breast"), UserFoodLog.MealType.LUNCH, 1.0, today.minusDays(5));
        saveFoodLog(user, foods.get("Brown Rice"), UserFoodLog.MealType.LUNCH, 1.5, today.minusDays(5));
        saveFoodLog(user, foods.get("Salmon Salad"), UserFoodLog.MealType.DINNER, 1.0, today.minusDays(5));

        saveFoodLog(user, foods.get("Oatmeal Bowl"), UserFoodLog.MealType.BREAKFAST, 1.0, today.minusDays(4));
        saveFoodLog(user, foods.get("Banana"), UserFoodLog.MealType.BREAKFAST, 1.0, today.minusDays(4));
        saveFoodLog(user, foods.get("Chicken Breast"), UserFoodLog.MealType.LUNCH, 1.2, today.minusDays(4));
        saveFoodLog(user, foods.get("Brown Rice"), UserFoodLog.MealType.LUNCH, 1.0, today.minusDays(4));
        saveFoodLog(user, foods.get("Greek Yogurt"), UserFoodLog.MealType.SNACK, 1.0, today.minusDays(4));
        saveFoodLog(user, foods.get("Salmon Salad"), UserFoodLog.MealType.DINNER, 1.0, today.minusDays(4));

        saveFoodLog(user, foods.get("Egg Sandwich"), UserFoodLog.MealType.BREAKFAST, 1.0, today.minusDays(3));
        saveFoodLog(user, foods.get("Banana"), UserFoodLog.MealType.SNACK, 1.0, today.minusDays(3));
        saveFoodLog(user, foods.get("Chicken Breast"), UserFoodLog.MealType.LUNCH, 1.0, today.minusDays(3));
        saveFoodLog(user, foods.get("Brown Rice"), UserFoodLog.MealType.LUNCH, 1.0, today.minusDays(3));
        saveFoodLog(user, foods.get("Salmon Salad"), UserFoodLog.MealType.DINNER, 0.8, today.minusDays(3));

        saveFoodLog(user, foods.get("Oatmeal Bowl"), UserFoodLog.MealType.BREAKFAST, 1.0, today.minusDays(2));
        saveFoodLog(user, foods.get("Greek Yogurt"), UserFoodLog.MealType.SNACK, 1.0, today.minusDays(2));
        saveFoodLog(user, foods.get("Chicken Breast"), UserFoodLog.MealType.LUNCH, 1.0, today.minusDays(2));
        saveFoodLog(user, foods.get("Brown Rice"), UserFoodLog.MealType.LUNCH, 0.8, today.minusDays(2));
        saveFoodLog(user, foods.get("Salmon Salad"), UserFoodLog.MealType.DINNER, 1.0, today.minusDays(2));

        saveFoodLog(user, foods.get("Egg Sandwich"), UserFoodLog.MealType.BREAKFAST, 1.0, today.minusDays(1));
        saveFoodLog(user, foods.get("Banana"), UserFoodLog.MealType.SNACK, 1.0, today.minusDays(1));
        saveFoodLog(user, foods.get("Chicken Breast"), UserFoodLog.MealType.LUNCH, 1.3, today.minusDays(1));
        saveFoodLog(user, foods.get("Brown Rice"), UserFoodLog.MealType.LUNCH, 1.2, today.minusDays(1));
        saveFoodLog(user, foods.get("Greek Yogurt"), UserFoodLog.MealType.SNACK, 1.0, today.minusDays(1));
        saveFoodLog(user, foods.get("Salmon Salad"), UserFoodLog.MealType.DINNER, 1.0, today.minusDays(1));

        saveFoodLog(user, foods.get("Oatmeal Bowl"), UserFoodLog.MealType.BREAKFAST, 1.0, today);
        saveFoodLog(user, foods.get("Banana"), UserFoodLog.MealType.BREAKFAST, 1.0, today);
        saveFoodLog(user, foods.get("Chicken Breast"), UserFoodLog.MealType.LUNCH, 1.0, today);
        saveFoodLog(user, foods.get("Brown Rice"), UserFoodLog.MealType.LUNCH, 1.0, today);
        saveFoodLog(user, foods.get("Greek Yogurt"), UserFoodLog.MealType.SNACK, 1.0, today);
    }

    private void saveFoodLog(User user, Food food, UserFoodLog.MealType mealType, Double servingQty, LocalDate logDate) {
        userFoodLogRepository.save(UserFoodLog.builder()
                .user(user)
                .food(food)
                .mealType(mealType)
                .servingQty(servingQty)
                .totalCalories(0.0)
                .logDate(logDate)
                .build());
    }

    private void seedWaterLogs(User user) {
        LocalDate today = LocalDate.now();
        saveWaterLog(user, 450, today.minusDays(2), 7, 30);
        saveWaterLog(user, 600, today.minusDays(2), 13, 15);
        saveWaterLog(user, 500, today.minusDays(2), 19, 10);

        saveWaterLog(user, 400, today.minusDays(1), 8, 0);
        saveWaterLog(user, 550, today.minusDays(1), 12, 45);
        saveWaterLog(user, 600, today.minusDays(1), 18, 20);

        saveWaterLog(user, 500, today, 7, 45);
        saveWaterLog(user, 650, today, 12, 10);
    }

    private void saveWaterLog(User user, int amountMl, LocalDate date, int hour, int minute) {
        userWaterLogRepository.save(UserWaterLog.builder()
                .user(user)
                .amountMl(amountMl)
                .logTime(LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), hour, minute))
                .logDate(date)
                .build());
    }

    private void seedStepLogs(User user) {
        LocalDate today = LocalDate.now();
        saveStepLog(user, 6200, today.minusDays(6));
        saveStepLog(user, 8100, today.minusDays(5));
        saveStepLog(user, 9300, today.minusDays(4));
        saveStepLog(user, 7400, today.minusDays(3));
        saveStepLog(user, 10250, today.minusDays(2));
        saveStepLog(user, 8800, today.minusDays(1));
        saveStepLog(user, 5600, today);
    }

    private void seedWeightLogs(Long userId) {
        jdbcTemplate.update("DELETE FROM weight_logs WHERE user_id = ?", userId);

        insertWeightLog(userId, 79.4, 78.8, "Week 1 progress", LocalDateTime.now().minusDays(21).withHour(7).withMinute(30).withSecond(0).withNano(0));
        insertWeightLog(userId, 78.8, 78.2, "Week 2 progress", LocalDateTime.now().minusDays(14).withHour(7).withMinute(20).withSecond(0).withNano(0));
        insertWeightLog(userId, 78.2, 77.6, "Week 3 progress", LocalDateTime.now().minusDays(7).withHour(7).withMinute(10).withSecond(0).withNano(0));
        insertWeightLog(userId, 77.6, 77.2, "Recent check-in", LocalDateTime.now().minusDays(3).withHour(7).withMinute(15).withSecond(0).withNano(0));
        insertWeightLog(userId, 77.2, 76.9, "Latest weigh-in", LocalDateTime.now().minusDays(1).withHour(7).withMinute(5).withSecond(0).withNano(0));
    }

    private void insertWeightLog(Long userId, Double oldWeight, Double newWeight, String note, LocalDateTime loggedAt) {
        jdbcTemplate.update(
                "INSERT INTO weight_logs (user_id, old_weight, new_weight, change_amount, note, logged_at) VALUES (?, ?, ?, ?, ?, ?)",
                userId,
                oldWeight,
                newWeight,
                newWeight - oldWeight,
                note,
                loggedAt
        );
    }

    private void saveStepLog(User user, int stepCount, LocalDate logDate) {
        stepLogRepository.save(StepLog.builder()
                .user(user)
                .stepCount(stepCount)
                .logDate(logDate)
                .build());
    }

    private void refreshDailySummaries(Long userId) {
        LocalDate today = LocalDate.now();
        for (int i = 0; i <= 6; i++) {
            dailySummaryService.buildAndSave(userId, today.minusDays(i));
        }
    }
}
