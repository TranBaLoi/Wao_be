package com.example.wao_be.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

public class WorkoutLogDto {

    @Data
    public static class Request {
        private Long exerciseId;  // null nếu tập theo chương trình
        private Long programId;   // null nếu tập bài lẻ

        @NotNull @Positive
        private Integer durationMin;

        private Double caloriesBurned; // tự tính nếu không truyền

        @NotNull
        private LocalDate logDate;

        private String note;
    }

    @Data
    public static class Response {
        private Long id;
        private Long userId;
        private Long exerciseId;
        private String exerciseName;
        private Long programId;
        private String programName;
        private Integer durationMin;
        private Double caloriesBurned;
        private LocalDate logDate;
        private String note;
    }
}

