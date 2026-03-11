package com.example.wao_be.repository;

import com.example.wao_be.entity.ProgramExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramExerciseRepository extends JpaRepository<ProgramExercise, Long> {
    List<ProgramExercise> findByProgramIdOrderByOrderIndexAsc(Long programId);
}

