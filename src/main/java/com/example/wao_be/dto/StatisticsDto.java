/*
 * Bài làm của Nguyễn Hải Nam-B22DCCN558
 * DTO cho module thống kê dinh dưỡng, chuỗi biểu đồ và ghi log cân nặng.
 */
// phan cua nam
package com.example.wao_be.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

public class StatisticsDto {

    /** Kiểu gom dữ liệu khi thống kê: theo ngày, theo tuần hoặc theo tháng. */
    public enum GroupBy {
        DAY,
        WEEK,
        MONTH
    }

    /** Response tổng dinh dưỡng của một ngày. */
    @Data
    public static class DailyNutritionResponse {
        private Long userId;
        private LocalDate date;
        private Double totalCalories;
        private Double totalProtein;
        private Double totalCarbs;
        private Double totalFat;
    }

    /** Một điểm dữ liệu dinh dưỡng trên biểu đồ. */
    @Data
    public static class NutritionPoint {
        private LocalDate bucketDate;
        private Double totalCalories;
        private Double totalProtein;
        private Double totalCarbs;
        private Double totalFat;
    }

    /** Response chuỗi dinh dưỡng trong một khoảng thời gian. */
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

    /** Một điểm dữ liệu cân nặng theo ngày. */
    @Data
    public static class WeightPoint {
        private LocalDate bucketDate;
        private Double startWeight;
        private Double endWeight;
        private Double changeAmount;
        private Integer logCount;
    }

    /** Response chuỗi cân nặng dùng cho biểu đồ xu hướng. */
    @Data
    public static class WeightSeriesResponse {
        private Long userId;
        private LocalDate from;
        private LocalDate to;
        private GroupBy groupBy;
        private Double overallChange;
        private List<WeightPoint> points;
    }

    //namthem
    /** Request frontend gửi lên khi người dùng cập nhật cân nặng. */
    @Data
    public static class CreateWeightLogRequest {
        private LocalDate date;
        private Double newWeight;
        private String note;
    }

    //namthem
    /** Response sau khi backend tạo log cân nặng và cập nhật profile thành công. */
    @Data
    public static class WeightLogUpdateResponse {
        private Long logId;
        private Long userId;
        private LocalDate date;
        private Double oldWeight;
        private Double newWeight;
        private Double changeAmount;
        private Double currentProfileWeight;
        private String note;
        private Double latestKnownWeight;
        private LocalDate latestKnownDate;
    }

    //namthem
    /** Response cho biết cân nặng gần nhất đang lấy từ weight log hay health profile. */
    @Data
    public static class LatestWeightInfoResponse {
        private Long userId;
        private Double latestKnownWeight;
        private LocalDate latestKnownDate;
        private String source;
    }
}
