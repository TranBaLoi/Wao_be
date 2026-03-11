package com.example.wao_be.repository;

import com.example.wao_be.entity.UserHealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserHealthProfileRepository extends JpaRepository<UserHealthProfile, Long> {
    List<UserHealthProfile> findByUserIdOrderByRecordedAtDesc(Long userId);
    Optional<UserHealthProfile> findFirstByUserIdOrderByRecordedAtDesc(Long userId);
}

