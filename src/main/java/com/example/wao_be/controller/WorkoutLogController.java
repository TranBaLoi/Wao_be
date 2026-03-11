package com.example.wao_be.controller;

import com.example.wao_be.dto.WorkoutLogDto;
import com.example.wao_be.service.DailySummaryService;
import com.example.wao_be.service.WorkoutLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/workout-logs")
@RequiredArgsConstructor
public class WorkoutLogController {

    private final WorkoutLogService workoutLogService;
    private final DailySummaryService dailySummaryService;

    /** POST /api/users/{userId}/workout-logs */
    @PostMapping
    public ResponseEntity<WorkoutLogDto.Response> log(
            @PathVariable Long userId,
            @Valid @RequestBody WorkoutLogDto.Request req) {
        WorkoutLogDto.Response response = workoutLogService.log(userId, req);
        dailySummaryService.buildAndSave(userId, req.getLogDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** GET /api/users/{userId}/workout-logs?date=yyyy-MM-dd */
    @GetMapping
    public ResponseEntity<List<WorkoutLogDto.Response>> getByDate(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(workoutLogService.getByUserAndDate(userId, date));
    }

    /** DELETE /api/users/{userId}/workout-logs/{logId} */
    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId,
                                       @PathVariable Long logId) {
        workoutLogService.delete(logId);
        dailySummaryService.buildAndSave(userId, LocalDate.now());
        return ResponseEntity.noContent().build();
    }
}

