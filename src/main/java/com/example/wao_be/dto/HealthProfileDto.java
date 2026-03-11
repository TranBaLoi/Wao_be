package com.example.wao_be.dto;

import com.example.wao_be.entity.UserHealthProfile;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

public class HealthProfileDto {

    @Data
    public static class Request {
        @NotNull
        private UserHealthProfile.Gender gender;

        @NotNull
        private LocalDate dob;

        @NotNull
        private Double heightCm;

        @NotNull
        private Double weightKg;

        @NotNull
        private UserHealthProfile.ActivityLevel activityLevel;

        @NotNull
        private UserHealthProfile.GoalType goalType;
    }

    @Data
    public static class Response {
        private Long id;
        private Long userId;
        private UserHealthProfile.Gender gender;
        private LocalDate dob;
        private Double heightCm;
        private Double weightKg;
        private UserHealthProfile.ActivityLevel activityLevel;
        private UserHealthProfile.GoalType goalType;
        private Double targetCalories;
    }
}

