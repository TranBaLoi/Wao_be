package com.example.wao_be.controller;

import com.example.wao_be.dto.WorkoutLogDto;
import com.example.wao_be.service.DailySummaryService;
import com.example.wao_be.service.WorkoutLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/workout-logs")
@RequiredArgsConstructor
public class WorkoutLogController {

    private final WorkoutLogService workoutLogService;
    private final DailySummaryService dailySummaryService;

    @PostMapping
    public ResponseEntity<WorkoutLogDto.Response> log(
            @PathVariable Long userId,
            @Valid @RequestBody WorkoutLogDto.Request req) {
        WorkoutLogDto.Response response = workoutLogService.log(userId, req);
        dailySummaryService.buildAndSave(userId, response.getLogDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WorkoutLogDto.Response>> getByDate(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(workoutLogService.getByUserAndDate(userId, date));
    }

    @GetMapping("/history")
    public ResponseEntity<List<WorkoutLogDto.Response>> getHistory(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(workoutLogService.getByUserAndDateRange(userId, from, to));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<WorkoutLogDto.SummaryResponse>> getSummary(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(workoutLogService.getSummary(userId, from, to));
    }

    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId,
                                       @PathVariable Long logId) {
        LocalDate deletedLogDate = workoutLogService.delete(userId, logId);
        dailySummaryService.buildAndSave(userId, deletedLogDate);
        return ResponseEntity.noContent().build();
    }
}
