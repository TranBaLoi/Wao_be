// sua de tranh config voi voi loi
package com.example.wao_be.service;

import com.example.wao_be.dto.DailyCalorieBreakdownDto;
import com.example.wao_be.dto.HealthProfileDto;
import com.example.wao_be.entity.User;
import com.example.wao_be.entity.UserHealthProfile;
import com.example.wao_be.entity.WeightLog;
import com.example.wao_be.repository.UserHealthProfileRepository;
import com.example.wao_be.repository.WeightLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class HealthProfileService {

    private final UserHealthProfileRepository profileRepository;
    private final WeightLogRepository weightLogRepository;
    private final UserService userService;

    public HealthProfileDto.Response create(Long userId, HealthProfileDto.Request req) {
        User user = userService.findById(userId);
        Optional<UserHealthProfile> latestProfile = profileRepository.findFirstByUserIdOrderByRecordedAtDesc(userId);

        UserHealthProfile profile = UserHealthProfile.builder()
                .user(user)
                .gender(req.getGender())
                .dob(req.getDob())
                .heightCm(req.getHeightCm())
                .weightKg(req.getWeightKg())
                .activityLevel(req.getActivityLevel())
                .goalType(req.getGoalType())
                .desiredWeightKg(req.getDesiredWeightKg())
                .targetDays(req.getTargetDays())
                .preferenceVector(req.getPreferenceVector())
                .build();
        // targetCalories và dailyCalories sẽ tự được tính trong @PrePersist

        UserHealthProfile savedProfile = profileRepository.save(profile);
        saveWeightLogIfNeeded(user, latestProfile.orElse(null), savedProfile);
        return toResponse(savedProfile);
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
                .stream()
                .map(this::toResponse)
                .toList();
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
        r.setDesiredWeightKg(p.getDesiredWeightKg());
        r.setTargetDays(p.getTargetDays());
        r.setTargetCalories(p.getTargetCalories());
        r.setDailyCalories(p.getDailyCalories());
        r.setPreferenceVector(p.getPreferenceVector());

        // Danh gia muc do kho (EASY/MEDIUM/HARD) dua tren dailyCalories
        if (p.getDailyCalories() != null) {
            r.setDailyCalorieBreakdown(DailyCalorieBreakdownDto.fromDailyCalories(p.getDailyCalories()));
        }

        return r;
    }

    private void saveWeightLogIfNeeded(User user, UserHealthProfile previousProfile, UserHealthProfile currentProfile) {
        Double newWeight = currentProfile.getWeightKg();
        if (newWeight == null) {
            return;
        }

        Double previousWeight = previousProfile != null ? previousProfile.getWeightKg() : null;
        if (previousWeight != null && Objects.equals(previousWeight, newWeight)) {
            return;
        }

        WeightLog weightLog = WeightLog.builder()
                .user(user)
                .oldWeight(previousWeight != null ? previousWeight : newWeight)
                .newWeight(newWeight)
                .note(previousWeight == null ? "Initial weight record" : "Weight updated from health profile")
                .build();

        weightLogRepository.save(weightLog);
    }
}
