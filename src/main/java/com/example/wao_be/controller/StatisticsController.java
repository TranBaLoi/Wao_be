// phan cua nam
package com.example.wao_be.controller;

import com.example.wao_be.dto.StatisticsDto;
import com.example.wao_be.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/users/{userId}/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/nutrition/daily")
    public ResponseEntity<StatisticsDto.DailyNutritionResponse> getDailyNutrition(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(statisticsService.getDailyNutrition(userId, date));
    }

    @GetMapping("/nutrition")
    public ResponseEntity<StatisticsDto.NutritionSeriesResponse> getNutritionSeries(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "DAY") StatisticsDto.GroupBy groupBy) {
        return ResponseEntity.ok(statisticsService.getNutritionSeries(userId, from, to, groupBy));
    }

    @GetMapping("/weight")
    public ResponseEntity<StatisticsDto.WeightSeriesResponse> getWeightSeries(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "DAY") StatisticsDto.GroupBy groupBy) {
        return ResponseEntity.ok(statisticsService.getWeightSeries(userId, from, to, groupBy));
    }
}
