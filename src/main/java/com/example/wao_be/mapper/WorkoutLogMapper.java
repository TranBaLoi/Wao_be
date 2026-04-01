package com.example.wao_be.mapper;

import com.example.wao_be.dto.WorkoutLogDto;
import com.example.wao_be.entity.UserWorkoutLog;
import org.springframework.stereotype.Component;

@Component
public class WorkoutLogMapper {

    public WorkoutLogDto.Response toResponse(UserWorkoutLog log) {
        WorkoutLogDto.Response response = new WorkoutLogDto.Response();
        response.setId(log.getId());
        response.setUserId(log.getUser().getId());
        response.setActivityType(log.getActivityType());
        response.setDurationMin(log.getDurationMin());
        response.setCaloriesBurned(log.getCaloriesBurned());
        response.setDistanceMeters(log.getDistanceMeters());
        response.setAvgSpeedKmh(log.getAvgSpeedKmh());
        response.setMaxSpeedKmh(log.getMaxSpeedKmh());
        response.setStepCount(log.getStepCount());
        response.setAvgHeartRate(log.getAvgHeartRate());
        response.setMaxHeartRate(log.getMaxHeartRate());
        response.setCaloriesSource(log.getCaloriesSource());
        response.setDistanceSource(log.getDistanceSource());
        response.setStepSource(log.getStepSource());
        response.setHeartRateSource(log.getHeartRateSource());
        response.setLogDate(log.getLogDate());
        response.setStartedAt(log.getStartedAt());
        response.setEndedAt(log.getEndedAt());
        response.setNote(log.getNote());
        if (log.getExercise() != null) {
            response.setExerciseId(log.getExercise().getId());
            response.setExerciseName(log.getExercise().getName());
        }
        if (log.getProgram() != null) {
            response.setProgramId(log.getProgram().getId());
            response.setProgramName(log.getProgram().getName());
        }
        response.setWorkoutType(log.getActivityType());
        response.setCreatedAt(log.getCreatedAt());
        return response;
    }
}
