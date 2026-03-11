package com.example.wao_be.dto;

import com.example.wao_be.entity.MealPlan;
import com.example.wao_be.entity.UserFoodLog;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

public class MealPlanDto {

    @Data
    public static class Request {
        @NotBlank
        private String name;

        private String description;

        @NotNull
        private MealPlan.MealPlanType type;

        /** Chỉ cần khi type = USER_CUSTOM */
        private Long userId;

        /** Danh sách món ăn trong meal plan */
        private List<FoodItem> foods;

        @Data
        public static class FoodItem {
            @NotNull
            private Long foodId;

            @NotNull
            private UserFoodLog.MealType mealType;

            @NotNull @Positive
            private Double servingQty;
        }
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private Double totalCalories;
        private MealPlan.MealPlanType type;
        private Long userId;        // null nếu SYSTEM_SUGGESTION
        private String userName;    // null nếu SYSTEM_SUGGESTION
        private List<FoodItemResponse> foods;

        @Data
        public static class FoodItemResponse {
            private Long id;
            private Long foodId;
            private String foodName;
            private UserFoodLog.MealType mealType;
            private Double servingQty;
            private Double calories;
        }
    }
}

