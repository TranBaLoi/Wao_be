package com.example.wao_be.dto;

import com.example.wao_be.entity.UserFoodLog;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

public class FoodLogDto {

    @Data
    public static class Request {
        @NotNull
        private Long foodId;

        @NotNull
        private UserFoodLog.MealType mealType;

        @NotNull @Positive
        private Double servingQty;

        @NotNull
        private LocalDate logDate;
    }

    @Data
    public static class Response {
        private Long id;
        private Long userId;
        private Long foodId;
        private String foodName;
        private UserFoodLog.MealType mealType;
        private Double servingQty;
        private Double totalCalories;
        private LocalDate logDate;
    }
}

