package com.example.wao_be.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

        private String featureVector;
        private String suitableMealTypes;

        private String ingredients;
        private String containsAllergens;
    }

    @Data
    public static class FormRequest {
        @NotBlank
        private String name;
        private String servingSize;

        @NotNull
        @Positive
        private Double calories;
        private Double protein;
        private Double carbs;
        private Double fat;

        private String featureVector;
        private String suitableMealTypes;

        private String ingredients;
        private String containsAllergens;
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

        private String featureVector;
        private String suitableMealTypes;

        private String ingredients;
        private String containsAllergens;

        /** Danh sach URL anh da upload len Cloudinary */
        private List<String> imageUrls;
    }
}
