package com.example.wao_be.service;

import com.example.wao_be.dto.WorkoutLogDto;
import com.example.wao_be.entity.Exercise;
import com.example.wao_be.entity.User;
import com.example.wao_be.entity.UserWorkoutLog;
import com.example.wao_be.entity.WorkoutProgram;
import com.example.wao_be.repository.UserWorkoutLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public WorkoutLogDto.Response log(Long userId, WorkoutLogDto.Request req) {
        User user = userService.findById(userId);

        Exercise exercise = null;
        WorkoutProgram program = null;

        if (req.getExerciseId() != null) {
            exercise = exerciseService.findById(req.getExerciseId());
        }
        if (req.getProgramId() != null) {
            program = programService.findById(req.getProgramId());
        }
        if (exercise == null && program == null) {
            throw new IllegalArgumentException("Either exerciseId or programId must be provided.");
        }

        UserWorkoutLog log = UserWorkoutLog.builder()
                .user(user)
                .exercise(exercise)
                .program(program)
                .durationMin(req.getDurationMin())
                .caloriesBurned(req.getCaloriesBurned()) // auto-computed if null via @PrePersist
                .logDate(req.getLogDate())
                .note(req.getNote())
                .build();

        return toResponse(workoutLogRepository.save(log));
    }

    @Transactional(readOnly = true)
    public List<WorkoutLogDto.Response> getByUserAndDate(Long userId, LocalDate date) {
        return workoutLogRepository.findByUserIdAndLogDate(userId, date)
                .stream().map(this::toResponse).toList();
    }

    public void delete(Long logId) {
        if (!workoutLogRepository.existsById(logId)) {
            throw new EntityNotFoundException("WorkoutLog not found: " + logId);
        }
        workoutLogRepository.deleteById(logId);
    }

    private WorkoutLogDto.Response toResponse(UserWorkoutLog l) {
        WorkoutLogDto.Response r = new WorkoutLogDto.Response();
        r.setId(l.getId());
        r.setUserId(l.getUser().getId());
        r.setDurationMin(l.getDurationMin());
        r.setCaloriesBurned(l.getCaloriesBurned());
        r.setLogDate(l.getLogDate());
        r.setNote(l.getNote());
        if (l.getExercise() != null) {
            r.setExerciseId(l.getExercise().getId());
            r.setExerciseName(l.getExercise().getName());
        }
        if (l.getProgram() != null) {
            r.setProgramId(l.getProgram().getId());
            r.setProgramName(l.getProgram().getName());
        }
        return r;
    }
}

