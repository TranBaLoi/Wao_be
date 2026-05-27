/*
 * Bài làm của Nguyễn Hải Nam-B22DCCN558
 * Controller cung cấp các API thống kê dinh dưỡng và theo dõi cân nặng cho ứng dụng mobile.
 */
// phan cua nam
package com.example.wao_be.controller;

import com.example.wao_be.dto.StatisticsDto;
import com.example.wao_be.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Nhận request từ Android cho module thống kê, sau đó chuyển toàn bộ nghiệp vụ sang StatisticsService.
 */
@RestController
@RequestMapping("/api/users/{userId}/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * API lấy tổng calories, protein, carbs và fat của một user trong một ngày cụ thể.
     */
    @GetMapping("/nutrition/daily")
    public ResponseEntity<StatisticsDto.DailyNutritionResponse> getDailyNutrition(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(statisticsService.getDailyNutrition(userId, date));
    }

    /**
     * API lấy chuỗi dữ liệu dinh dưỡng theo khoảng ngày để frontend vẽ biểu đồ ngày/tuần/tháng.
     */
    @GetMapping("/nutrition")
    public ResponseEntity<StatisticsDto.NutritionSeriesResponse> getNutritionSeries(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "DAY") StatisticsDto.GroupBy groupBy) {
        return ResponseEntity.ok(statisticsService.getNutritionSeries(userId, from, to, groupBy));
    }

    /**
     * API lấy chuỗi cân nặng theo ngày, gồm cân nặng đầu/cuối ngày và mức thay đổi.
     */
    @GetMapping("/weight")
    public ResponseEntity<StatisticsDto.WeightSeriesResponse> getWeightSeries(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "DAY") StatisticsDto.GroupBy groupBy) {
        return ResponseEntity.ok(statisticsService.getWeightSeries(userId, from, to, groupBy));
    }

    //namthem
    /**
     * API trả về cân nặng gần nhất, ưu tiên bản ghi weight_logs và fallback về hồ sơ sức khỏe.
     */
    @GetMapping("/weight/latest")
    public ResponseEntity<StatisticsDto.LatestWeightInfoResponse> getLatestWeightInfo(
            @PathVariable Long userId) {
        return ResponseEntity.ok(statisticsService.getLatestWeightInfo(userId));
    }

    //namthem
    /**
     * API tạo log cân nặng mới cho ngày hiện tại và đồng bộ cân nặng về health profile.
     */
    @PostMapping("/weight/logs")
    public ResponseEntity<StatisticsDto.WeightLogUpdateResponse> createWeightLog(
            @PathVariable Long userId,
            @RequestBody StatisticsDto.CreateWeightLogRequest request) {
        return ResponseEntity.ok(statisticsService.createWeightLog(userId, request));
    }
}
