package com.example.wao_be.repository;

import com.example.wao_be.entity.UserFoodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserFoodLogRepository extends JpaRepository<UserFoodLog, Long> {

    List<UserFoodLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    List<UserFoodLog> findByUserIdAndLogDateBetween(Long userId, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(f.totalCalories), 0) FROM UserFoodLog f " +
           "WHERE f.user.id = :userId AND f.logDate = :date")
    Double sumCaloriesByUserIdAndLogDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}

