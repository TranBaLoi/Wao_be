package com.example.wao_be.controller;

import com.example.wao_be.dto.ExerciseDto;
import com.example.wao_be.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    /** POST /api/exercises */
    @PostMapping
    public ResponseEntity<ExerciseDto.Response> create(@Valid @RequestBody ExerciseDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(exerciseService.create(req));
    }

    /** GET /api/exercises?name= */
    @GetMapping
    public ResponseEntity<List<ExerciseDto.Response>> search(
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(exerciseService.search(name));
    }

    /** GET /api/exercises/category/{categoryId} */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ExerciseDto.Response>> getByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(exerciseService.getByCategory(categoryId));
    }

    /** GET /api/exercises/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ExerciseDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseService.getById(id));
    }

    /** DELETE /api/exercises/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        exerciseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

