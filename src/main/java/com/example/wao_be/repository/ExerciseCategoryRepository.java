package com.example.wao_be.repository;

import com.example.wao_be.entity.ExerciseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseCategoryRepository extends JpaRepository<ExerciseCategory, Long> {
}

