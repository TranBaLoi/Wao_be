package com.example.wao_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ExerciseCategory category;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "video_url")
    private String videoUrl;

    /**
     * Calo đốt cháy trên mỗi phút tập (kcal/min).
     * Có thể dùng METs thay thế nếu cần độ chính xác cao hơn.
     */
    @Column(name = "calories_per_min")
    private Double caloriesPerMin;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL)
    private List<ProgramExercise> programExercises;

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL)
    private List<UserWorkoutLog> workoutLogs;
}

