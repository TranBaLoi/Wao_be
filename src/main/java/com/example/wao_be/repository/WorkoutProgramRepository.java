package com.example.wao_be.repository;

import com.example.wao_be.entity.WorkoutProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkoutProgramRepository extends JpaRepository<WorkoutProgram, Long> {
    List<WorkoutProgram> findByLevel(WorkoutProgram.ProgramLevel level);
}

