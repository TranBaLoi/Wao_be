package com.example.wao_be.controller;

import com.example.wao_be.dto.FoodLogDto;
import com.example.wao_be.service.DailySummaryService;
import com.example.wao_be.service.FoodLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/food-logs")
@RequiredArgsConstructor
public class FoodLogController {

    private final FoodLogService foodLogService;
    private final DailySummaryService dailySummaryService;

    /** POST /api/users/{userId}/food-logs */
    @PostMapping
    public ResponseEntity<FoodLogDto.Response> log(
            @PathVariable Long userId,
            @Valid @RequestBody FoodLogDto.Request req) {
        FoodLogDto.Response response = foodLogService.log(userId, req);
        // Cập nhật DailySummary sau khi log thức ăn
        dailySummaryService.buildAndSave(userId, req.getLogDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** GET /api/users/{userId}/food-logs?date=yyyy-MM-dd */
    @GetMapping
    public ResponseEntity<List<FoodLogDto.Response>> getByDate(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(foodLogService.getByUserAndDate(userId, date));
    }

    /** DELETE /api/users/{userId}/food-logs/{logId} */
    @DeleteMapping("/{logId}")
    public ResponseEntity<Void> delete(@PathVariable Long userId,
                                       @PathVariable Long logId) {
        foodLogService.delete(logId);
        dailySummaryService.buildAndSave(userId, LocalDate.now());
        return ResponseEntity.noContent().build();
    }
}

