package com.example.wao_be.service;

import com.example.wao_be.dto.WorkoutLogDto;
import com.example.wao_be.entity.Exercise;
import com.example.wao_be.entity.User;
import com.example.wao_be.entity.UserWorkoutLog;
import com.example.wao_be.entity.UserWorkoutLog.WorkoutDataSource;
import com.example.wao_be.entity.WorkoutProgram;
import com.example.wao_be.mapper.WorkoutLogMapper;
import com.example.wao_be.repository.UserWorkoutLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkoutLogService {

    private final UserWorkoutLogRepository workoutLogRepository;
    private final UserService userService;
    private final ExerciseService exerciseService;
    private final WorkoutProgramService programService;
    private final WorkoutLogMapper workoutLogMapper;

    public WorkoutLogDto.Response log(Long userId, WorkoutLogDto.Request req) {
        User user = userService.findById(userId);

        validateRequest(req);

        Exercise exercise = null;
        WorkoutProgram program = null;

        if (req.getExerciseId() != null) {
            exercise = exerciseService.findById(req.getExerciseId());
        }
        if (req.getProgramId() != null) {
            program = programService.findById(req.getProgramId());
        }
        if (exercise == null && program == null && req.getActivityType() == null) {
            throw new IllegalArgumentException("One of exerciseId, programId, or activityType must be provided.");
        }

        Integer durationMin = resolveDurationMin(req);
        LocalDate logDate = resolveLogDate(req);
        Double caloriesBurned = resolveCaloriesBurned(req, exercise, durationMin);

        UserWorkoutLog log = UserWorkoutLog.builder()
                .user(user)
                .exercise(exercise)
                .program(program)
                .activityType(req.getActivityType())
                .durationMin(durationMin)
                .caloriesBurned(caloriesBurned)
                .distanceMeters(req.getDistanceMeters())
                .avgSpeedKmh(req.getAvgSpeedKmh())
                .maxSpeedKmh(req.getMaxSpeedKmh())
                .stepCount(req.getStepCount())
                .avgHeartRate(req.getAvgHeartRate())
                .maxHeartRate(req.getMaxHeartRate())
                .caloriesSource(resolveCaloriesSource(req, caloriesBurned, exercise, durationMin))
                .distanceSource(resolveDistanceSource(req))
                .heartRateSource(resolveHeartRateSource(req))
                .logDate(logDate)
                .startedAt(req.getStartedAt())
                .endedAt(req.getEndedAt())
                .note(req.getNote())
                .build();

        return workoutLogMapper.toResponse(workoutLogRepository.save(log));
    }

    @Transactional(readOnly = true)
    public List<WorkoutLogDto.Response> getByUserAndDate(Long userId, LocalDate date) {
        return workoutLogRepository.findByUserIdAndLogDate(userId, date)
                .stream()
                .map(workoutLogMapper::toResponse)
                .toList();
    }

    public LocalDate delete(Long userId, Long logId) {
        UserWorkoutLog log = workoutLogRepository.findByIdAndUserId(logId, userId)
                .orElseThrow(() -> new EntityNotFoundException("WorkoutLog not found: " + logId));
        LocalDate logDate = log.getLogDate();
        workoutLogRepository.delete(log);
        return logDate;
    }

    private void validateRequest(WorkoutLogDto.Request req) {
        if (req.getExerciseId() != null && req.getProgramId() != null) {
            throw new IllegalArgumentException("Only one of exerciseId or programId can be provided.");
        }
        if (req.getStartedAt() != null && req.getEndedAt() == null
                || req.getStartedAt() == null && req.getEndedAt() != null) {
            throw new IllegalArgumentException("startedAt and endedAt must be provided together.");
        }
        if (req.getStartedAt() != null && !req.getEndedAt().isAfter(req.getStartedAt())) {
            throw new IllegalArgumentException("endedAt must be after startedAt.");
        }
        if (req.getDurationMin() == null && (req.getStartedAt() == null || req.getEndedAt() == null)) {
            throw new IllegalArgumentException("durationMin is required unless both startedAt and endedAt are provided.");
        }
        if (req.getDurationMin() == null && req.getStartedAt() != null
                && Duration.between(req.getStartedAt(), req.getEndedAt()).toMinutes() <= 0) {
            throw new IllegalArgumentException("startedAt and endedAt must be at least 1 minute apart.");
        }
        if (req.getLogDate() == null && req.getStartedAt() == null) {
            throw new IllegalArgumentException("logDate is required unless startedAt is provided.");
        }
        if (req.getAvgSpeedKmh() != null && req.getMaxSpeedKmh() != null
                && req.getAvgSpeedKmh() > req.getMaxSpeedKmh()) {
            throw new IllegalArgumentException("avgSpeedKmh cannot be greater than maxSpeedKmh.");
        }
        if (req.getAvgHeartRate() != null && req.getMaxHeartRate() != null
                && req.getAvgHeartRate() > req.getMaxHeartRate()) {
            throw new IllegalArgumentException("avgHeartRate cannot be greater than maxHeartRate.");
        }
        if (hasTrackingMetrics(req) && req.getActivityType() == null) {
            throw new IllegalArgumentException("activityType is required when tracking metrics are provided.");
        }
    }

    private boolean hasTrackingMetrics(WorkoutLogDto.Request req) {
        return req.getDistanceMeters() != null
                || req.getAvgSpeedKmh() != null
                || req.getMaxSpeedKmh() != null
                || req.getStepCount() != null
                || req.getAvgHeartRate() != null
                || req.getMaxHeartRate() != null
                || req.getDistanceSource() != null
                || req.getHeartRateSource() != null
                || req.getStartedAt() != null
                || req.getEndedAt() != null;
    }

    private Integer resolveDurationMin(WorkoutLogDto.Request req) {
        if (req.getDurationMin() != null) {
            return req.getDurationMin();
        }
        return Math.toIntExact(Duration.between(req.getStartedAt(), req.getEndedAt()).toMinutes());
    }

    private LocalDate resolveLogDate(WorkoutLogDto.Request req) {
        if (req.getLogDate() != null) {
            return req.getLogDate();
        }
        return req.getStartedAt().toLocalDate();
    }

    private Double resolveCaloriesBurned(WorkoutLogDto.Request req, Exercise exercise, Integer durationMin) {
        if (req.getCaloriesBurned() != null) {
            return req.getCaloriesBurned();
        }
        if (exercise != null && exercise.getCaloriesPerMin() != null && durationMin != null) {
            return exercise.getCaloriesPerMin() * durationMin;
        }
        return null;
    }

    private WorkoutDataSource resolveCaloriesSource(
            WorkoutLogDto.Request req,
            Double caloriesBurned,
            Exercise exercise,
            Integer durationMin) {
        if (req.getCaloriesSource() != null) {
            return req.getCaloriesSource();
        }
        if (caloriesBurned == null) {
            return null;
        }
        if (req.getCaloriesBurned() != null) {
            return WorkoutDataSource.ESTIMATED;
        }
        if (exercise != null && exercise.getCaloriesPerMin() != null && durationMin != null) {
            return WorkoutDataSource.ESTIMATED;
        }
        return null;
    }

    private WorkoutDataSource resolveDistanceSource(WorkoutLogDto.Request req) {
        if (req.getDistanceSource() != null) {
            return req.getDistanceSource();
        }
        return req.getDistanceMeters() != null ? WorkoutDataSource.MANUAL : null;
    }

    private WorkoutDataSource resolveHeartRateSource(WorkoutLogDto.Request req) {
        if (req.getHeartRateSource() != null) {
            return req.getHeartRateSource();
        }
        return (req.getAvgHeartRate() != null || req.getMaxHeartRate() != null)
                ? WorkoutDataSource.MANUAL
                : null;
    }
}
