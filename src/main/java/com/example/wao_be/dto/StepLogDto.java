package com.example.wao_be.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

public class StepLogDto {

    @Data
    public static class Request {
        @NotNull @Positive
        private Integer stepCount;

        @NotNull
        private LocalDate logDate;
    }

    @Data
    public static class Response {
        private Long id;
        private Long userId;
        private Integer stepCount;
        private LocalDate logDate;
    }
}

