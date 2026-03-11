package com.example.wao_be.repository;

import com.example.wao_be.entity.DailySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailySummaryRepository extends JpaRepository<DailySummary, DailySummary.DailySummaryId> {

    Optional<DailySummary> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    List<DailySummary> findByUserIdAndLogDateBetweenOrderByLogDateAsc(
            Long userId, LocalDate from, LocalDate to);
}

