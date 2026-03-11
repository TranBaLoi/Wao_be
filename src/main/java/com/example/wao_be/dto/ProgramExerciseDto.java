package com.example.wao_be.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class ProgramExerciseDto {

    @Data
    public static class Request {
        @NotNull
        private Long exerciseId;

        @NotNull
        private Integer orderIndex;

        @NotNull
        private Integer sets;

        @NotNull
        private Integer reps;

        private Integer restTimeSec;
    }

    @Data
    public static class Response {
        private Long id;
        private Long exerciseId;
        private String exerciseName;
        private Integer orderIndex;
        private Integer sets;
        private Integer reps;
        private Integer restTimeSec;
    }
}

