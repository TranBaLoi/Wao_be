package com.example.wao_be.service;

import com.example.wao_be.dto.ProgramExerciseDto;
import com.example.wao_be.dto.WorkoutProgramDto;
import com.example.wao_be.entity.Exercise;
import com.example.wao_be.entity.ProgramExercise;
import com.example.wao_be.entity.WorkoutProgram;
import com.example.wao_be.repository.WorkoutProgramRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WorkoutProgramService {

    private final WorkoutProgramRepository programRepository;
    private final ExerciseService exerciseService;

    public WorkoutProgramDto.Response create(WorkoutProgramDto.Request req) {
        WorkoutProgram program = WorkoutProgram.builder()
                .name(req.getName())
                .level(req.getLevel())
                .estimatedDuration(req.getEstimatedDuration())
                .description(req.getDescription())
                .build();

        if (req.getExercises() != null) {
            List<ProgramExercise> items = new ArrayList<>();
            for (ProgramExerciseDto.Request pe : req.getExercises()) {
                Exercise ex = exerciseService.findById(pe.getExerciseId());
                items.add(ProgramExercise.builder()
                        .program(program)
                        .exercise(ex)
                        .orderIndex(pe.getOrderIndex())
                        .sets(pe.getSets())
                        .reps(pe.getReps())
                        .restTimeSec(pe.getRestTimeSec())
                        .build());
            }
            program.setProgramExercises(items);
        }
        return toResponse(programRepository.save(program));
    }

    @Transactional(readOnly = true)
    public List<WorkoutProgramDto.Response> getAll() {
        return programRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<WorkoutProgramDto.Response> getByLevel(WorkoutProgram.ProgramLevel level) {
        return programRepository.findByLevel(level).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WorkoutProgramDto.Response getById(Long id) {
        return toResponse(findById(id));
    }

    public WorkoutProgram findById(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("WorkoutProgram not found: " + id));
    }

    public void delete(Long id) {
        programRepository.deleteById(id);
    }

    private WorkoutProgramDto.Response toResponse(WorkoutProgram p) {
        WorkoutProgramDto.Response r = new WorkoutProgramDto.Response();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setLevel(p.getLevel());
        r.setEstimatedDuration(p.getEstimatedDuration());
        r.setDescription(p.getDescription());
        if (p.getProgramExercises() != null) {
            r.setExercises(p.getProgramExercises().stream().map(pe -> {
                ProgramExerciseDto.Response per = new ProgramExerciseDto.Response();
                per.setId(pe.getId());
                per.setExerciseId(pe.getExercise().getId());
                per.setExerciseName(pe.getExercise().getName());
                per.setOrderIndex(pe.getOrderIndex());
                per.setSets(pe.getSets());
                per.setReps(pe.getReps());
                per.setRestTimeSec(pe.getRestTimeSec());
                return per;
            }).toList());
        }
        return r;
    }
}

