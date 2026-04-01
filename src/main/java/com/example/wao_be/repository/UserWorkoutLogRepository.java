package com.example.wao_be.repository;

import com.example.wao_be.entity.UserWorkoutLog;
import com.example.wao_be.entity.UserWorkoutLog.WorkoutDataSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserWorkoutLogRepository extends JpaRepository<UserWorkoutLog, Long> {

    List<UserWorkoutLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    List<UserWorkoutLog> findByUserIdAndLogDateBetween(Long userId, LocalDate from, LocalDate to);

    Optional<UserWorkoutLog> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(w.caloriesBurned), 0) FROM UserWorkoutLog w " +
           "WHERE w.user.id = :userId AND w.logDate = :date")
    Double sumCaloriesBurnedByUserIdAndLogDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(w.caloriesBurned), 0) FROM UserWorkoutLog w " +
           "WHERE w.user.id = :userId AND w.logDate = :date AND w.caloriesSource = :source")
    Double sumCaloriesBurnedByUserIdAndLogDateAndCaloriesSource(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("source") WorkoutDataSource source);

    @Query("SELECT COALESCE(SUM(w.distanceMeters), 0) FROM UserWorkoutLog w " +
           "WHERE w.user.id = :userId AND w.logDate = :date")
    Double sumDistanceMetersByUserIdAndLogDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(w.distanceMeters), 0) FROM UserWorkoutLog w " +
           "WHERE w.user.id = :userId AND w.logDate = :date AND w.distanceSource = :source")
    Double sumDistanceMetersByUserIdAndLogDateAndDistanceSource(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("source") WorkoutDataSource source);

    @Query("SELECT COALESCE(SUM(w.stepCount), 0) FROM UserWorkoutLog w " +
           "WHERE w.user.id = :userId AND w.logDate = :date")
    Integer sumStepCountByUserIdAndLogDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
