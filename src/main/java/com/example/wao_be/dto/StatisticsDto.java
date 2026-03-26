// phan cua nam
package com.example.wao_be.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

public class StatisticsDto {

    public enum GroupBy {
        DAY,
        WEEK,
        MONTH
    }

    @Data
    public static class DailyNutritionResponse {
        private Long userId;
        private LocalDate date;
        private Double totalCalories;
        private Double totalProtein;
        private Double totalCarbs;
        private Double totalFat;
    }

    @Data
    public static class NutritionPoint {
        private LocalDate bucketDate;
        private Double totalCalories;
        private Double totalProtein;
        private Double totalCarbs;
        private Double totalFat;
    }

    @Data
    public static class NutritionSeriesResponse {
        private Long userId;
        private LocalDate from;
        private LocalDate to;
        private GroupBy groupBy;
        private Double totalCalories;
        private Double totalProtein;
        private Double totalCarbs;
        private Double totalFat;
        private List<NutritionPoint> points;
    }

    @Data
    public static class WeightPoint {
        private LocalDate bucketDate;
        private Double startWeight;
        private Double endWeight;
        private Double changeAmount;
        private Integer logCount;
    }

    @Data
    public static class WeightSeriesResponse {
        private Long userId;
        private LocalDate from;
        private LocalDate to;
        private GroupBy groupBy;
        private Double overallChange;
        private List<WeightPoint> points;
    }
}
