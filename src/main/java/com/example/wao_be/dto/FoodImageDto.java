package com.example.wao_be.dto;

import lombok.Data;

import java.time.LocalDateTime;

public class FoodImageDto {

    @Data
    public static class Response {
        private Long id;
        private Long foodId;
        private String fileName;
        private String contentType;
        private Long fileSize;
        private byte[] data;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}

