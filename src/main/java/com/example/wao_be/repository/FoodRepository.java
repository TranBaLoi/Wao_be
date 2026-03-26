package com.example.wao_be.repository;

import com.example.wao_be.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByNameContainingIgnoreCase(String name);
    List<Food> findByIsVerified(Boolean isVerified);

    List<Food> findBySuitableMealTypesContaining(String mealType);

    @Query("SELECT f FROM Food f WHERE f.calories >= :minCalo AND f.calories <= :maxCalo AND f.suitableMealTypes LIKE %:mealType%")
    List<Food> findCandidateFoods(@Param("minCalo") double minCalo, @Param("maxCalo") double maxCalo, @Param("mealType") String mealType);
}
