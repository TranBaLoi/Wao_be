package com.example.wao_be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "meal_plan_foods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPlanFood {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    /** Bữa ăn trong meal plan: BREAKFAST / LUNCH / DINNER / SNACK */
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 20)
    private UserFoodLog.MealType mealType;

    /** Số khẩu phần gợi ý */
    @Column(name = "serving_qty", nullable = false)
    @Builder.Default
    private Double servingQty = 1.0;

    /** Calo của món này trong meal plan = food.calories * servingQty */
    @Column(name = "calories")
    private Double calories;

    @PrePersist
    @PreUpdate
    public void computeCalories() {
        if (food != null && servingQty != null) {
            calories = food.getCalories() * servingQty;
        }
    }
}

