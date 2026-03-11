package com.example.wao_be.repository;

import com.example.wao_be.entity.StepLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StepLogRepository extends JpaRepository<StepLog, Long> {

    Optional<StepLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    List<StepLog> findByUserIdAndLogDateBetween(Long userId, LocalDate from, LocalDate to);

    @Query("SELECT COALESCE(SUM(s.stepCount), 0) FROM StepLog s " +
           "WHERE s.user.id = :userId AND s.logDate = :date")
    Integer sumStepsByUserIdAndLogDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}

