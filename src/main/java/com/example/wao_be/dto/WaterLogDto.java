package com.example.wao_be.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WaterLogDto {

    @Data
    public static class Request {
        @NotNull @Positive
        private Integer amountMl;

        @NotNull
        private LocalDateTime logTime;
    }

    @Data
    public static class Response {
        private Long id;
        private Long userId;
        private Integer amountMl;
        private LocalDateTime logTime;
        private LocalDate logDate;
    }
}

