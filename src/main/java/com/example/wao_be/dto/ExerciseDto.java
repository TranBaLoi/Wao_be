package com.example.wao_be.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

public class ExerciseDto {

    @Data
    public static class Request {
        @NotBlank
        private String name;
        private Long categoryId;
        private String videoUrl;
        private Double caloriesPerMin;
        private String description;
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        private Long categoryId;
        private String categoryName;
        private String videoUrl;
        private Double caloriesPerMin;
        private String description;
    }
}

