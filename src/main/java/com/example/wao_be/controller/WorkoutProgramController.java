package com.example.wao_be.controller;

import com.example.wao_be.dto.WorkoutProgramDto;
import com.example.wao_be.entity.WorkoutProgram;
import com.example.wao_be.service.WorkoutProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workout-programs")
@RequiredArgsConstructor
public class WorkoutProgramController {

    private final WorkoutProgramService programService;

    /** POST /api/workout-programs */
    @PostMapping
    public ResponseEntity<WorkoutProgramDto.Response> create(
            @Valid @RequestBody WorkoutProgramDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(programService.create(req));
    }

    /** GET /api/workout-programs */
    @GetMapping
    public ResponseEntity<List<WorkoutProgramDto.Response>> getAll(
            @RequestParam(required = false) WorkoutProgram.ProgramLevel level) {
        if (level != null) {
            return ResponseEntity.ok(programService.getByLevel(level));
        }
        return ResponseEntity.ok(programService.getAll());
    }

    /** GET /api/workout-programs/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<WorkoutProgramDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(programService.getById(id));
    }

    /** DELETE /api/workout-programs/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        programService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

