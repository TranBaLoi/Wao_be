package com.example.wao_be.repository;

import com.example.wao_be.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    List<Food> findByNameContainingIgnoreCase(String name);
    List<Food> findByIsVerified(Boolean isVerified);
}

