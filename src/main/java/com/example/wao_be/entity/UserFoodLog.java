package com.example.wao_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_food_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFoodLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    /** Bữa ăn: BREAKFAST / LUNCH / DINNER / SNACK */
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private MealType mealType;

    /** Số khẩu phần (ví dụ: 1.5 chén) */
    @Column(name = "serving_qty", nullable = false)
    private Double servingQty;

    /** Tổng calo = food.calories * servingQty */
    @Column(name = "total_calories", nullable = false)
    private Double totalCalories;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @PrePersist
    @PreUpdate
    public void computeCalories() {
        if (food != null && servingQty != null) {
            totalCalories = food.getCalories() * servingQty;
        }
    }

    public enum MealType {
        BREAKFAST,  // Sáng
        LUNCH,      // Trưa
        DINNER,     // Tối
        SNACK       // Phụ
    }
}

