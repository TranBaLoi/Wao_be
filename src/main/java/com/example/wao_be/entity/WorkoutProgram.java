package com.example.wao_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "workout_programs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ProgramLevel level;

    /** Thời lượng ước tính (phút) */
    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<ProgramExercise> programExercises;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL)
    private List<UserWorkoutLog> workoutLogs;

    public enum ProgramLevel {
        BEGINNER, INTERMEDIATE, PRO
    }
}

