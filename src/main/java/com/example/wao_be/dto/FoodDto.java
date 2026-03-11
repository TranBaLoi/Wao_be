package com.example.wao_be.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

public class FoodDto {

    @Data
    public static class Request {
        @NotBlank
        private String name;
        private String servingSize;

        @NotNull @Positive
        private Double calories;
        private Double protein;
        private Double carbs;
        private Double fat;
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        private String servingSize;
        private Double calories;
        private Double protein;
        private Double carbs;
        private Double fat;
        private Boolean isVerified;
    }
}

