package com.example.wao_be.dto;

import com.example.wao_be.entity.UserWorkoutLog.ActivityType;
import com.example.wao_be.entity.UserWorkoutLog.WorkoutDataSource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkoutLogDto {

    public enum SummaryGroupType {
        EXERCISE,
        PROGRAM,
        ACTIVITY
    }

    @Data
    public static class Request {
        private Long exerciseId;
        private Long programId;
        private ActivityType activityType;
        private ActivityType workoutType;

        @Positive
        private Integer durationMin;

        @PositiveOrZero
        private Double caloriesBurned;

        @PositiveOrZero
        private Double distanceMeters;

        @PositiveOrZero
        private Double avgSpeedKmh;

        @PositiveOrZero
        private Double maxSpeedKmh;

        @PositiveOrZero
        private Integer stepCount;

        @Min(1)
        @Max(300)
        private Integer avgHeartRate;

        @Min(1)
        @Max(300)
        private Integer maxHeartRate;

        private WorkoutDataSource caloriesSource;
        private WorkoutDataSource distanceSource;
        private WorkoutDataSource stepSource;
        private WorkoutDataSource heartRateSource;
        private LocalDate logDate;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
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
        private ActivityType activityType;
        private ActivityType workoutType;
        private Integer durationMin;
        private Double caloriesBurned;
        private Double distanceMeters;
        private Double avgSpeedKmh;
        private Double maxSpeedKmh;
        private Integer stepCount;
        private Integer avgHeartRate;
        private Integer maxHeartRate;
        private WorkoutDataSource caloriesSource;
        private WorkoutDataSource distanceSource;
        private WorkoutDataSource stepSource;
        private WorkoutDataSource heartRateSource;
        private LocalDate logDate;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private String note;
        private LocalDateTime createdAt;
    }

    @Data
    public static class SummaryResponse {
        private SummaryGroupType groupType;
        private String groupKey;
        private String displayName;
        private Long exerciseId;
        private String exerciseName;
        private Long programId;
        private String programName;
        private ActivityType activityType;
        private ActivityType workoutType;
        private Integer totalSessions;
        private Integer totalDurationMin;
        private Double totalCaloriesBurned;
        private Double totalDistanceMeters;
        private Integer totalStepCount;
        private LocalDateTime lastSessionAt;
    }
}
