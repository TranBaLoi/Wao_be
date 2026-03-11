package com.example.wao_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "foods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    /** Mô tả khẩu phần, ví dụ: "100g", "1 chén" */
    @Column(name = "serving_size", length = 100)
    private String servingSize;

    /** Calo trên mỗi khẩu phần (kcal) */
    @Column(nullable = false)
    private Double calories;

    /** Protein (g) */
    private Double protein;

    /** Carbohydrate (g) */
    private Double carbs;

    /** Fat (g) */
    private Double fat;

    /**
     * true  = admin đã xác minh
     * false = do user tự tạo
     */
    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL)
    private List<UserFoodLog> foodLogs;

    /** Danh sách meal plan chứa món ăn này */
    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL)
    private List<MealPlanFood> mealPlanFoods;
}

