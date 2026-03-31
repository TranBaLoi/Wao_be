// Nam them respository 
package com.example.wao_be.repository;

import com.example.wao_be.entity.WeightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeightLogRepository extends JpaRepository<WeightLog, Long> {
    List<WeightLog> findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(
            Long userId,
            LocalDateTime from,
            LocalDateTime to);

    //namthem
    Optional<WeightLog> findFirstByUserIdAndLoggedAtLessThanEqualOrderByLoggedAtDesc(
            Long userId,
            LocalDateTime loggedAt);

    //namthem
    Optional<WeightLog> findFirstByUserIdOrderByLoggedAtDesc(Long userId);

    //namthem
    boolean existsByUserIdAndLoggedAtBetween(
            Long userId,
            LocalDateTime from,
            LocalDateTime to);
}
