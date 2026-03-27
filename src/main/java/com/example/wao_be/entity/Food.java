package com.example.wao_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
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

    @Column(name = "feature_vector", columnDefinition = "TEXT")
    private String featureVector;

    @Column(name = "ingredients", columnDefinition = "TEXT")
    private String ingredients;

    @Column(name = "contains_allergens")
    private String containsAllergens;

    @Column(name = "suitable_meal_types")
    private String suitableMealTypes;

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL)
    private List<UserFoodLog> foodLogs;

    /** Danh sách meal plan chứa món ăn này */
    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL)
    private List<MealPlanFood> mealPlanFoods;


    @ElementCollection
    @CollectionTable(name = "food_images", joinColumns = @JoinColumn(name = "foods_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;
}
