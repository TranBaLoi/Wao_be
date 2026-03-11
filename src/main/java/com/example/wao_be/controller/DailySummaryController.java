package com.example.wao_be.controller;

import com.example.wao_be.dto.DailySummaryDto;
import com.example.wao_be.service.DailySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/daily-summaries")
@RequiredArgsConstructor
public class DailySummaryController {

    private final DailySummaryService dailySummaryService;

    /**
     * GET /api/users/{userId}/daily-summaries/today
     * Lấy tổng kết hôm nay (dashboard nhanh)
     */
    @GetMapping("/today")
    public ResponseEntity<DailySummaryDto> getToday(@PathVariable Long userId) {
        DailySummaryDto dto = dailySummaryService.buildAndSave(userId, LocalDate.now());
        return ResponseEntity.ok(dto);
    }

    /**
     * GET /api/users/{userId}/daily-summaries?date=yyyy-MM-dd
     * Lấy tổng kết một ngày cụ thể
     */
    @GetMapping
    public ResponseEntity<DailySummaryDto> getByDate(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(dailySummaryService.getByUserAndDate(userId, date));
    }

    /**
     * GET /api/users/{userId}/daily-summaries/history?from=yyyy-MM-dd&to=yyyy-MM-dd
     * Lấy lịch sử theo khoảng thời gian (dùng cho chart/thống kê)
     */
    @GetMapping("/history")
    public ResponseEntity<List<DailySummaryDto>> getHistory(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(dailySummaryService.getHistory(userId, from, to));
    }

    /**
     * POST /api/users/{userId}/daily-summaries/refresh?date=yyyy-MM-dd
     * Tính lại tổng kết cho một ngày (manual refresh)
     */
    @PostMapping("/refresh")
    public ResponseEntity<DailySummaryDto> refresh(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return ResponseEntity.ok(dailySummaryService.buildAndSave(userId, targetDate));
    }
}

