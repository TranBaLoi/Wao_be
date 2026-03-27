package com.example.wao_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

// cần phải thêm log vào mới thống kê được
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
     * Cân nặng mong muốn sau khi hoàn thành mục tiêu
     * - GAIN_WEIGHT: desiredWeight > weightKg
     * - LOSE_WEIGHT: desiredWeight < weightKg
     * - MAINTAIN: desiredWeight ≈ weightKg (trong 2%)
     */
    @Column(name = "desired_weight_kg")
    private Double desiredWeightKg;

    /**
     * Số ngày để đạt mục tiêu (tính từ ngày hôm nay)
     */
    @Column(name = "target_days")
    private Integer targetDays;

    /**
     * Tong calories/ngay ma user CAN AN VAO de dat duoc muc tieu.
     * Cong thuc: TDEE +/- daily_calories
     */
    @Column(name = "target_calories")
    private Double targetCalories;

    /**
     * Luong calories can BU(tru)/THANG(du) moi ngay so voi TDEE.
     * Cong thuc: (|desiredWeightKg - weightKg| * 7700) / targetDays
     */
    @Column(name = "daily_calories")
    private Double dailyCalories;

    @Column(name = "preference_vector", columnDefinition = "TEXT")
    private String preferenceVector; // Ví dụ: "1.0, 0.5, 0.2, ..."

    @Column(name = "allergies")
    private String allergies; // Ví dụ: "PEANUT, MILK"

    @CreationTimestamp
    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum ActivityLevel {
        SEDENTARY, // Ít vận động
        LIGHTLY_ACTIVE, // Nhẹ nhàng 1-3 ngày/tuần
        MODERATELY_ACTIVE, // Vừa phải 3-5 ngày/tuần
        VERY_ACTIVE, // Nhiều 6-7 ngày/tuần
        EXTRA_ACTIVE // Cực kỳ nhiều
    }

    public enum GoalType {
        LOSE_WEIGHT, // Giảm cân
        GAIN_WEIGHT, // Tăng cân
        MAINTAIN // Duy trì
    }

    /**
     * Tinh toan TDEE va calories theo muc tieu can nang.
     */
    @PrePersist
    @PreUpdate
    public void calculateTDEE() {
        if (weightKg == null || desiredWeightKg == null || goalType == null || targetDays == null || heightCm == null || dob == null || activityLevel == null) {
            return;
        }

        validateDesiredWeight();

        if (targetDays <= 0) {
            throw new IllegalArgumentException("targetDays must be greater than 0");
        }

        // Tinh muc thang du/tham hut moi ngay (dailyCalories)
        double deltaKg = desiredWeightKg - weightKg;
        double totalCaloriesDiff = Math.abs(deltaKg) * 7700.0;
        dailyCalories = totalCaloriesDiff / targetDays; // Tinh luong calo can bu/tru moi ngay

        // Tinh BMR va TDEE tu can nang HIEN TAI
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

        // Tinh tong luong calo nap vao moi ngay (targetCalories)
        targetCalories = switch (goalType) {
            case LOSE_WEIGHT -> tdee - dailyCalories; // Giam can thi phai an it hon TDEE
            case GAIN_WEIGHT -> tdee + dailyCalories; // Tang can thi phai an nhieu hon TDEE
            case MAINTAIN -> tdee;                    // Duy tri thi an bang TDEE
        };
    }

    /**
     * Validate desiredWeight theo goalType
     */
    private void validateDesiredWeight() {
        if (weightKg == null || desiredWeightKg == null)
            return;

        switch (goalType) {
            case GAIN_WEIGHT:
                if (desiredWeightKg <= weightKg) {
                    throw new IllegalArgumentException(
                            "Desired weight must be greater than current weight for GAIN_WEIGHT goal");
                }
                break;
            case LOSE_WEIGHT:
                if (desiredWeightKg >= weightKg) {
                    throw new IllegalArgumentException(
                            "Desired weight must be less than current weight for LOSE_WEIGHT goal");
                }
                break;
            case MAINTAIN:
                double toleranceRange = weightKg * 0.02; // 2% tolerance
                if (Math.abs(desiredWeightKg - weightKg) > toleranceRange) {
                    throw new IllegalArgumentException(
                            "Desired weight must be within 2% of current weight for MAINTAIN goal");
                }
                break;
        }
    }
}
