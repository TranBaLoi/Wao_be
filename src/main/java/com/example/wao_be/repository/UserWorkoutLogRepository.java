package com.example.wao_be.repository;

import com.example.wao_be.entity.UserWorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserWorkoutLogRepository extends JpaRepository<UserWorkoutLog, Long> {

    List<UserWorkoutLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    List<UserWorkoutLog> findByUserIdAndLogDateBetween(Long userId, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(w.caloriesBurned), 0) FROM UserWorkoutLog w " +
           "WHERE w.user.id = :userId AND w.logDate = :date")
    Double sumCaloriesBurnedByUserIdAndLogDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}

