package com.example.wao_be.controller;

import com.example.wao_be.dto.StepLogDto;
import com.example.wao_be.service.DailySummaryService;
import com.example.wao_be.service.StepLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/step-logs")
@RequiredArgsConstructor
public class StepLogController {

    private final StepLogService stepLogService;
    private final DailySummaryService dailySummaryService;

    /** POST /api/users/{userId}/step-logs */
    @PostMapping
    public ResponseEntity<StepLogDto.Response> log(
            @PathVariable Long userId,
            @Valid @RequestBody StepLogDto.Request req) {
        StepLogDto.Response response = stepLogService.log(userId, req);
        dailySummaryService.buildAndSave(userId, req.getLogDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** GET /api/users/{userId}/step-logs?date=yyyy-MM-dd */
    @GetMapping("/date")
    public ResponseEntity<StepLogDto.Response> getByDate(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(stepLogService.getByUserAndDate(userId, date));
    }

    /** GET /api/users/{userId}/step-logs?from=yyyy-MM-dd&to=yyyy-MM-dd */
    @GetMapping
    public ResponseEntity<List<StepLogDto.Response>> getByRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(stepLogService.getByUserAndDateRange(userId, from, to));
    }
}

