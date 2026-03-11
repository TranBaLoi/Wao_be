package com.example.wao_be.controller;

import com.example.wao_be.dto.WaterLogDto;
import com.example.wao_be.service.DailySummaryService;
import com.example.wao_be.service.WaterLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/water-logs")
@RequiredArgsConstructor
public class WaterLogController {

    private final WaterLogService waterLogService;
    private final DailySummaryService dailySummaryService;

    /** POST /api/users/{userId}/water-logs */
    @PostMapping
    public ResponseEntity<WaterLogDto.Response> log(
            @PathVariable Long userId,
            @Valid @RequestBody WaterLogDto.Request req) {
        WaterLogDto.Response response = waterLogService.log(userId, req);
        dailySummaryService.buildAndSave(userId, req.getLogTime().toLocalDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** GET /api/users/{userId}/water-logs?date=yyyy-MM-dd */
    @GetMapping
    public ResponseEntity<List<WaterLogDto.Response>> getByDate(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(waterLogService.getByUserAndDate(userId, date));
    }

    /** GET /api/users/{userId}/water-logs/total?date=yyyy-MM-dd */
    @GetMapping("/total")
    public ResponseEntity<Integer> getTotal(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(waterLogService.getTotalWaterByUserAndDate(userId, date));
    }

    /** DELETE /api/users/{userId}/water-logs/{logId} */
    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId,
                                       @PathVariable Long logId) {
        waterLogService.delete(logId);
        dailySummaryService.buildAndSave(userId, LocalDate.now());
        return ResponseEntity.noContent().build();
    }
}

