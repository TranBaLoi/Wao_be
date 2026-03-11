package com.example.wao_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_health_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserHealthProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", length = 20)
    private ActivityLevel activityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", length = 20)
    private GoalType goalType;

    /**
     * Được tính toán tự động dựa trên công thức TDEE
     * (Total Daily Energy Expenditure)
     */
    @Column(name = "target_calories")
    private Double targetCalories;

    @CreationTimestamp
    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum ActivityLevel {
        SEDENTARY,          // Ít vận động
        LIGHTLY_ACTIVE,     // Nhẹ nhàng 1-3 ngày/tuần
        MODERATELY_ACTIVE,  // Vừa phải 3-5 ngày/tuần
        VERY_ACTIVE,        // Nhiều 6-7 ngày/tuần
        EXTRA_ACTIVE        // Cực kỳ nhiều
    }

    public enum GoalType {
        LOSE_WEIGHT,    // Giảm cân
        GAIN_WEIGHT,    // Tăng cân
        MAINTAIN        // Duy trì
    }

    /**
     * Tính TDEE tự động dựa trên Mifflin-St Jeor Equation
     * Gọi trước khi persist/update
     */
    @PrePersist
    @PreUpdate
    public void calculateTDEE() {
        if (weightKg == null || heightCm == null || dob == null
                || activityLevel == null || goalType == null) return;

        int age = LocalDate.now().getYear() - dob.getYear();
        double bmr;

        if (gender == Gender.FEMALE) {
            bmr = 10 * weightKg + 6.25 * heightCm - 5 * age - 161;
        } else {
            bmr = 10 * weightKg + 6.25 * heightCm - 5 * age + 5;
        }

        double multiplier = switch (activityLevel) {
            case SEDENTARY -> 1.2;
            case LIGHTLY_ACTIVE -> 1.375;
            case MODERATELY_ACTIVE -> 1.55;
            case VERY_ACTIVE -> 1.725;
            case EXTRA_ACTIVE -> 1.9;
        };

        double tdee = bmr * multiplier;

        targetCalories = switch (goalType) {
            case LOSE_WEIGHT -> tdee - 500;
            case GAIN_WEIGHT -> tdee + 500;
            case MAINTAIN -> tdee;
        };
    }
}

