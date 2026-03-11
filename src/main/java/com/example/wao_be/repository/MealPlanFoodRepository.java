package com.example.wao_be.repository;

import com.example.wao_be.entity.MealPlanFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealPlanFoodRepository extends JpaRepository<MealPlanFood, Long> {
    List<MealPlanFood> findByMealPlanId(Long mealPlanId);
    void deleteByMealPlanId(Long mealPlanId);
}

