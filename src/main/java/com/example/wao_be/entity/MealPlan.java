package com.example.wao_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meal_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_calories")
    private Double totalCalories;

    /**
     * SYSTEM_SUGGESTION: do hệ thống gợi ý (user = null)
     * USER_CUSTOM: do người dùng tự tạo (user != null)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MealPlanType type;

    /**
     * Quan hệ với User:
     * - USER_CUSTOM  → user != null (meal plan thuộc về user này)
     * - SYSTEM_SUGGESTION → user = null (áp dụng cho tất cả)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Quan hệ với Food qua bảng trung gian MealPlanFood:
     * Một meal plan chứa nhiều món ăn (có thể chia theo bữa)
     */
    @OneToMany(mappedBy = "mealPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MealPlanFood> mealPlanFoods = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Tự tính tổng calo từ tất cả món ăn trong meal plan
     */
    public void recalculateTotalCalories() {
        this.totalCalories = mealPlanFoods.stream()
                .mapToDouble(mpf -> mpf.getCalories() != null ? mpf.getCalories() : 0.0)
                .sum();
    }

    public enum MealPlanType {
        SYSTEM_SUGGESTION,
        USER_CUSTOM
    }
}
