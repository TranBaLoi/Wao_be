package com.example.wao_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    /**
     * Tập bài lẻ => có exercise_id, program_id = null
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    /**
     * Tập theo chương trình => có program_id, exercise_id = null
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private WorkoutProgram program;

    /** Thời gian tập thực tế (phút) */
    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    /** Calo đốt cháy thực tế */
    @Column(name = "calories_burned")
    private Double caloriesBurned;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(columnDefinition = "TEXT")
    private String note;

    @PrePersist
    @PreUpdate
    public void computeCaloriesBurned() {
        if (caloriesBurned == null && exercise != null
                && exercise.getCaloriesPerMin() != null && durationMin != null) {
            caloriesBurned = exercise.getCaloriesPerMin() * durationMin;
        }
    }
}

