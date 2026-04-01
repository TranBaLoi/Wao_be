package com.example.wao_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_workout_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWorkoutLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private WorkoutProgram program;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", length = 30)
    private ActivityType activityType;

    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    @Column(name = "calories_burned")
    private Double caloriesBurned;

    @Column(name = "distance_meters")
    private Double distanceMeters;

    @Column(name = "avg_speed_kmh")
    private Double avgSpeedKmh;

    @Column(name = "max_speed_kmh")
    private Double maxSpeedKmh;

    @Column(name = "step_count")
    private Integer stepCount;

    @Column(name = "avg_heart_rate")
    private Integer avgHeartRate;

    @Column(name = "max_heart_rate")
    private Integer maxHeartRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "calories_source", length = 30)
    private WorkoutDataSource caloriesSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "distance_source", length = 30)
    private WorkoutDataSource distanceSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_source", length = 30)
    private WorkoutDataSource stepSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "heart_rate_source", length = 30)
    private WorkoutDataSource heartRateSource;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    public void computeCaloriesBurned() {
        if (caloriesBurned == null && exercise != null
                && exercise.getCaloriesPerMin() != null && durationMin != null) {
            caloriesBurned = exercise.getCaloriesPerMin() * durationMin;
        }
    }

    public enum ActivityType {
        OUTDOOR_WALKING,
        OUTDOOR_RUNNING,
        INDOOR_RUNNING,
        CYCLING,
        OUTDOOR_CYCLING,
        OTHER
    }

    public enum WorkoutDataSource {
        GPS,
        HEALTH_CONNECT,
        SENSOR,
        ESTIMATED,
        MANUAL
    }
}
