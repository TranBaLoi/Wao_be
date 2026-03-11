package com.example.wao_be.repository;

import com.example.wao_be.entity.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    /** Lọc theo type: SYSTEM_SUGGESTION hoặc USER_CUSTOM */
    List<MealPlan> findByType(MealPlan.MealPlanType type);

    /** Lấy tất cả meal plan USER_CUSTOM của một user */
    List<MealPlan> findByUserId(Long userId);

    /** Lấy meal plan USER_CUSTOM của user theo type */
    List<MealPlan> findByTypeAndUserId(MealPlan.MealPlanType type, Long userId);
}

