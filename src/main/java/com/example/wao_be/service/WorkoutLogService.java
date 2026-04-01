package com.example.wao_be.service;

import com.example.wao_be.dto.WorkoutLogDto;
import com.example.wao_be.entity.Exercise;
import com.example.wao_be.entity.User;
import com.example.wao_be.entity.UserWorkoutLog;
import com.example.wao_be.entity.UserWorkoutLog.ActivityType;
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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkoutLogService {

    private final UserWorkoutLogRepository workoutLogRepository;
    private final UserService userService;
    private final ExerciseService exerciseService;
    private final WorkoutProgramService programService;
    private final WorkoutLogMapper workoutLogMapper;
    private static final Comparator<UserWorkoutLog> WORKOUT_LOG_COMPARATOR =
            Comparator.comparing(
                            WorkoutLogService::resolveSessionAt,
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .reversed()
                    .thenComparing(UserWorkoutLog::getId, Comparator.nullsLast(Comparator.reverseOrder()));

    public WorkoutLogDto.Response log(Long userId, WorkoutLogDto.Request req) {
        User user = userService.findById(userId);

        validateRequest(req);
        ActivityType activityType = resolveActivityType(req);

        Exercise exercise = null;
        WorkoutProgram program = null;

        if (req.getExerciseId() != null) {
            exercise = exerciseService.findById(req.getExerciseId());
        }
        if (req.getProgramId() != null) {
            program = programService.findById(req.getProgramId());
        }
        if (exercise == null && program == null && activityType == null) {
            throw new IllegalArgumentException("One of exerciseId, programId, activityType, or workoutType must be provided.");
        }

        Integer durationMin = resolveDurationMin(req);
        LocalDate logDate = resolveLogDate(req);
        Double caloriesBurned = resolveCaloriesBurned(req, exercise, durationMin);

        UserWorkoutLog log = UserWorkoutLog.builder()
                .user(user)
                .exercise(exercise)
                .program(program)
                .activityType(activityType)
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
                .stepSource(resolveStepSource(req))
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
                .sorted(WORKOUT_LOG_COMPARATOR)
                .map(workoutLogMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutLogDto.Response> getByUserAndDateRange(Long userId, LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        return workoutLogRepository.findByUserIdAndLogDateBetween(userId, from, to)
                .stream()
                .sorted(WORKOUT_LOG_COMPARATOR)
                .map(workoutLogMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutLogDto.SummaryResponse> getSummary(Long userId, LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        Map<String, WorkoutLogDto.SummaryResponse> summaryByGroup = new LinkedHashMap<>();
        List<UserWorkoutLog> logs = workoutLogRepository.findByUserIdAndLogDateBetween(userId, from, to);

        for (UserWorkoutLog log : logs) {
            String groupKey = resolveGroupKey(log);
            WorkoutLogDto.SummaryResponse summary = summaryByGroup.computeIfAbsent(
                    groupKey, ignored -> initializeSummary(log, groupKey));
            mergeSummary(summary, log);
        }

        return summaryByGroup.values().stream()
                .sorted(Comparator.comparing(
                                WorkoutLogDto.SummaryResponse::getLastSessionAt,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed()
                        .thenComparing(
                                WorkoutLogDto.SummaryResponse::getDisplayName,
                                Comparator.nullsLast(Comparator.naturalOrder())))
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
        resolveActivityType(req);
        if (req.getExerciseId() != null && req.getProgramId() != null) {
            throw new IllegalArgumentException("Only one of exerciseId or programId can be provided.");
        }
        if (req.getStartedAt() != null && req.getEndedAt() == null
                || req.getStartedAt() == null && req.getEndedAt() != null) {
            throw new IllegalArgumentException("startedAt and endedAt must be provided together.");
        }
        if (req.getStartedAt() != null && !req.getEndedAt().isAfter(req.getStartedAt())) {
            throw new IllegalArgumentException("startedAt must be before endedAt.");
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
        if (hasTrackingMetrics(req) && resolveActivityType(req) == null) {
            throw new IllegalArgumentException("activityType or workoutType is required when tracking metrics are provided.");
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
                || req.getStepSource() != null
                || req.getHeartRateSource() != null
                || req.getStartedAt() != null
                || req.getEndedAt() != null;
    }

    private ActivityType resolveActivityType(WorkoutLogDto.Request req) {
        if (req.getActivityType() != null && req.getWorkoutType() != null
                && req.getActivityType() != req.getWorkoutType()) {
            throw new IllegalArgumentException("activityType and workoutType must match when both are provided.");
        }
        return req.getActivityType() != null ? req.getActivityType() : req.getWorkoutType();
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

    private WorkoutDataSource resolveStepSource(WorkoutLogDto.Request req) {
        if (req.getStepSource() != null) {
            return req.getStepSource();
        }
        return req.getStepCount() != null ? WorkoutDataSource.MANUAL : null;
    }

    private WorkoutDataSource resolveHeartRateSource(WorkoutLogDto.Request req) {
        if (req.getHeartRateSource() != null) {
            return req.getHeartRateSource();
        }
        return (req.getAvgHeartRate() != null || req.getMaxHeartRate() != null)
                ? WorkoutDataSource.MANUAL
                : null;
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be on or before to.");
        }
    }

    private WorkoutLogDto.SummaryResponse initializeSummary(UserWorkoutLog log, String groupKey) {
        WorkoutLogDto.SummaryResponse response = new WorkoutLogDto.SummaryResponse();
        response.setGroupType(resolveGroupType(log));
        response.setGroupKey(groupKey);
        response.setDisplayName(resolveDisplayName(log));
        response.setExerciseId(log.getExercise() != null ? log.getExercise().getId() : null);
        response.setExerciseName(log.getExercise() != null ? log.getExercise().getName() : null);
        response.setProgramId(log.getProgram() != null ? log.getProgram().getId() : null);
        response.setProgramName(log.getProgram() != null ? log.getProgram().getName() : null);
        response.setActivityType(log.getActivityType());
        response.setWorkoutType(log.getActivityType());
        response.setTotalSessions(0);
        response.setTotalDurationMin(0);
        response.setTotalCaloriesBurned(0D);
        response.setTotalDistanceMeters(0D);
        response.setTotalStepCount(0);
        response.setLastSessionAt(resolveSessionAt(log));
        return response;
    }

    private void mergeSummary(WorkoutLogDto.SummaryResponse summary, UserWorkoutLog log) {
        summary.setTotalSessions(summary.getTotalSessions() + 1);
        summary.setTotalDurationMin(summary.getTotalDurationMin() + safeInt(log.getDurationMin()));
        summary.setTotalCaloriesBurned(summary.getTotalCaloriesBurned() + safeDouble(log.getCaloriesBurned()));
        summary.setTotalDistanceMeters(summary.getTotalDistanceMeters() + safeDouble(log.getDistanceMeters()));
        summary.setTotalStepCount(summary.getTotalStepCount() + safeInt(log.getStepCount()));

        LocalDateTime sessionAt = resolveSessionAt(log);
        if (sessionAt != null && (summary.getLastSessionAt() == null || sessionAt.isAfter(summary.getLastSessionAt()))) {
            summary.setLastSessionAt(sessionAt);
        }
    }

    private WorkoutLogDto.SummaryGroupType resolveGroupType(UserWorkoutLog log) {
        if (log.getExercise() != null) {
            return WorkoutLogDto.SummaryGroupType.EXERCISE;
        }
        if (log.getProgram() != null) {
            return WorkoutLogDto.SummaryGroupType.PROGRAM;
        }
        return WorkoutLogDto.SummaryGroupType.ACTIVITY;
    }

    private String resolveGroupKey(UserWorkoutLog log) {
        if (log.getExercise() != null) {
            return "EXERCISE:" + log.getExercise().getId();
        }
        if (log.getProgram() != null) {
            return "PROGRAM:" + log.getProgram().getId();
        }
        if (log.getActivityType() != null) {
            return "ACTIVITY:" + log.getActivityType().name();
        }
        return "WORKOUT_LOG:" + log.getId();
    }

    private String resolveDisplayName(UserWorkoutLog log) {
        if (log.getExercise() != null) {
            return log.getExercise().getName();
        }
        if (log.getProgram() != null) {
            return log.getProgram().getName();
        }
        if (log.getActivityType() != null) {
            return log.getActivityType().name();
        }
        return "Workout #" + log.getId();
    }

    private static LocalDateTime resolveSessionAt(UserWorkoutLog log) {
        if (log.getStartedAt() != null) {
            return log.getStartedAt();
        }
        if (log.getEndedAt() != null) {
            return log.getEndedAt();
        }
        if (log.getCreatedAt() != null) {
            return log.getCreatedAt();
        }
        return log.getLogDate() != null ? log.getLogDate().atStartOfDay() : null;
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }

    private double safeDouble(Double value) {
        return value != null ? value : 0D;
    }
}
