package com.example.wao_be.service;

import com.example.wao_be.dto.HealthProfileDto;
import com.example.wao_be.entity.User;
import com.example.wao_be.entity.UserHealthProfile;
import com.example.wao_be.repository.UserHealthProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HealthProfileService {

    private final UserHealthProfileRepository profileRepository;
    private final UserService userService;

    public HealthProfileDto.Response create(Long userId, HealthProfileDto.Request req) {
        User user = userService.findById(userId);
        UserHealthProfile profile = UserHealthProfile.builder()
                .user(user)
                .gender(req.getGender())
                .dob(req.getDob())
                .heightCm(req.getHeightCm())
                .weightKg(req.getWeightKg())
                .activityLevel(req.getActivityLevel())
                .goalType(req.getGoalType())
                .build();
        // targetCalories sẽ tự được tính trong @PrePersist
        return toResponse(profileRepository.save(profile));
    }

    @Transactional(readOnly = true)
    public HealthProfileDto.Response getLatest(Long userId) {
        return profileRepository.findFirstByUserIdOrderByRecordedAtDesc(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("No health profile for user: " + userId));
    }

    @Transactional(readOnly = true)
    public List<HealthProfileDto.Response> getHistory(Long userId) {
        return profileRepository.findByUserIdOrderByRecordedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    private HealthProfileDto.Response toResponse(UserHealthProfile p) {
        HealthProfileDto.Response r = new HealthProfileDto.Response();
        r.setId(p.getId());
        r.setUserId(p.getUser().getId());
        r.setGender(p.getGender());
        r.setDob(p.getDob());
        r.setHeightCm(p.getHeightCm());
        r.setWeightKg(p.getWeightKg());
        r.setActivityLevel(p.getActivityLevel());
        r.setGoalType(p.getGoalType());
        r.setTargetCalories(p.getTargetCalories());
        return r;
    }
}

