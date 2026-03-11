package com.example.wao_be.repository;

import com.example.wao_be.entity.UserWaterLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserWaterLogRepository extends JpaRepository<UserWaterLog, Long> {

    List<UserWaterLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);

    @Query("SELECT COALESCE(SUM(w.amountMl), 0) FROM UserWaterLog w " +
           "WHERE w.user.id = :userId AND w.logDate = :date")
    Integer sumWaterByUserIdAndLogDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}

