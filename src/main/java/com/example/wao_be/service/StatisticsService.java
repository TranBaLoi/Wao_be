// phan cua nam
package com.example.wao_be.service;

import com.example.wao_be.dto.StatisticsDto;
import com.example.wao_be.entity.UserFoodLog;
import com.example.wao_be.entity.WeightLog;
import com.example.wao_be.repository.UserFoodLogRepository;
import com.example.wao_be.repository.WeightLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final UserFoodLogRepository userFoodLogRepository;
    private final WeightLogRepository weightLogRepository;
    private final UserService userService;

    public StatisticsDto.DailyNutritionResponse getDailyNutrition(Long userId, LocalDate date) {
        userService.findById(userId);

        NutritionAccumulator accumulator = new NutritionAccumulator();
        userFoodLogRepository.findByUserIdAndLogDate(userId, date)
                .forEach(accumulator::add);

        StatisticsDto.DailyNutritionResponse response = new StatisticsDto.DailyNutritionResponse();
        response.setUserId(userId);
        response.setDate(date);
        response.setTotalCalories(accumulator.totalCalories);
        response.setTotalProtein(accumulator.totalProtein);
        response.setTotalCarbs(accumulator.totalCarbs);
        response.setTotalFat(accumulator.totalFat);
        return response;
    }

    public StatisticsDto.NutritionSeriesResponse getNutritionSeries(
            Long userId,
            LocalDate from,
            LocalDate to,
            StatisticsDto.GroupBy groupBy) {
        userService.findById(userId);
        validateRange(from, to);

        Map<LocalDate, NutritionAccumulator> buckets = initNutritionBuckets(from, to, groupBy);
        userFoodLogRepository.findByUserIdAndLogDateBetween(userId, from, to)
                .forEach(log -> buckets.get(bucketDate(log.getLogDate(), groupBy)).add(log));

        List<StatisticsDto.NutritionPoint> points = new ArrayList<>();
        NutritionAccumulator totals = new NutritionAccumulator();

        for (Map.Entry<LocalDate, NutritionAccumulator> entry : buckets.entrySet()) {
            NutritionAccumulator accumulator = entry.getValue();
            totals.merge(accumulator);

            StatisticsDto.NutritionPoint point = new StatisticsDto.NutritionPoint();
            point.setBucketDate(entry.getKey());
            point.setTotalCalories(accumulator.totalCalories);
            point.setTotalProtein(accumulator.totalProtein);
            point.setTotalCarbs(accumulator.totalCarbs);
            point.setTotalFat(accumulator.totalFat);
            points.add(point);
        }

        StatisticsDto.NutritionSeriesResponse response = new StatisticsDto.NutritionSeriesResponse();
        response.setUserId(userId);
        response.setFrom(from);
        response.setTo(to);
        response.setGroupBy(groupBy);
        response.setTotalCalories(totals.totalCalories);
        response.setTotalProtein(totals.totalProtein);
        response.setTotalCarbs(totals.totalCarbs);
        response.setTotalFat(totals.totalFat);
        response.setPoints(points);
        return response;
    }

    public StatisticsDto.WeightSeriesResponse getWeightSeries(
            Long userId,
            LocalDate from,
            LocalDate to,
            StatisticsDto.GroupBy groupBy) {
        userService.findById(userId);
        validateRange(from, to);

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay().minusNanos(1);

        // nam them: can nang luon tra ve theo tung ngay, lay newWeight moi nhat cua ngay do
        Map<LocalDate, WeightAccumulator> buckets = initWeightBuckets(from, to);
        List<WeightLog> logs = weightLogRepository.findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(
                userId, fromDateTime, toDateTime);

        for (WeightLog log : logs) {
            LocalDate bucketKey = log.getLoggedAt().toLocalDate();
            buckets.computeIfAbsent(bucketKey, ignored -> new WeightAccumulator()).add(log);
        }

        List<StatisticsDto.WeightPoint> points = new ArrayList<>();
        Double firstWeight = null;
        Double lastWeight = null;

        for (Map.Entry<LocalDate, WeightAccumulator> entry : buckets.entrySet()) {
            WeightAccumulator accumulator = entry.getValue();

            StatisticsDto.WeightPoint point = new StatisticsDto.WeightPoint();
            point.setBucketDate(entry.getKey());
            point.setStartWeight(accumulator.startWeight);
            point.setEndWeight(accumulator.endWeight);
            point.setChangeAmount(accumulator.getChangeAmount());
            point.setLogCount(accumulator.logCount);
            points.add(point);

            if (accumulator.endWeight != null && firstWeight == null) {
                firstWeight = accumulator.endWeight;
            }
            if (accumulator.endWeight != null) {
                lastWeight = accumulator.endWeight;
            }
        }

        StatisticsDto.WeightSeriesResponse response = new StatisticsDto.WeightSeriesResponse();
        response.setUserId(userId);
        response.setFrom(from);
        response.setTo(to);
        response.setGroupBy(StatisticsDto.GroupBy.DAY);
        response.setOverallChange(firstWeight != null && lastWeight != null ? lastWeight - firstWeight : null);
        response.setPoints(points);
        return response;
    }

    private Map<LocalDate, NutritionAccumulator> initNutritionBuckets(
            LocalDate from,
            LocalDate to,
            StatisticsDto.GroupBy groupBy) {
        Map<LocalDate, NutritionAccumulator> buckets = new LinkedHashMap<>();
        LocalDate cursor = bucketDate(from, groupBy);
        LocalDate endBucket = bucketDate(to, groupBy);

        while (!cursor.isAfter(endBucket)) {
            buckets.put(cursor, new NutritionAccumulator());
            cursor = nextBucket(cursor, groupBy);
        }
        return buckets;
    }

    private Map<LocalDate, WeightAccumulator> initWeightBuckets(
            LocalDate from,
            LocalDate to) {
        Map<LocalDate, WeightAccumulator> buckets = new LinkedHashMap<>();
        LocalDate cursor = from;
        LocalDate endBucket = to;

        while (!cursor.isAfter(endBucket)) {
            buckets.put(cursor, new WeightAccumulator());
            cursor = cursor.plusDays(1);
        }
        return buckets;
    }

    private LocalDate bucketDate(LocalDate date, StatisticsDto.GroupBy groupBy) {
        return switch (groupBy) {
            case DAY -> date;
            case WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> date.withDayOfMonth(1);
        };
    }

    private LocalDate nextBucket(LocalDate bucketDate, StatisticsDto.GroupBy groupBy) {
        return switch (groupBy) {
            case DAY -> bucketDate.plusDays(1);
            case WEEK -> bucketDate.plusWeeks(1);
            case MONTH -> bucketDate.plusMonths(1);
        };
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }
    }

    private double safe(Double value) {
        return value != null ? value : 0.0;
    }

    private class NutritionAccumulator {
        private double totalCalories;
        private double totalProtein;
        private double totalCarbs;
        private double totalFat;

        private void add(UserFoodLog log) {
            double servingQty = safe(log.getServingQty());
            totalCalories += safe(log.getTotalCalories());
            totalProtein += safe(log.getFood().getProtein()) * servingQty;
            totalCarbs += safe(log.getFood().getCarbs()) * servingQty;
            totalFat += safe(log.getFood().getFat()) * servingQty;
        }

        private void merge(NutritionAccumulator other) {
            totalCalories += other.totalCalories;
            totalProtein += other.totalProtein;
            totalCarbs += other.totalCarbs;
            totalFat += other.totalFat;
        }
    }

    private static class WeightAccumulator {
        private Double startWeight;
        private Double endWeight;
        private int logCount;

        private void add(WeightLog log) {
            Double effectiveOldWeight = log.getOldWeight() != null ? log.getOldWeight() : log.getNewWeight();
            if (startWeight == null) {
                startWeight = effectiveOldWeight;
            }
            endWeight = log.getNewWeight();
            logCount++;
        }

        private Double getChangeAmount() {
            if (startWeight == null || endWeight == null) {
                return null;
            }
            return endWeight - startWeight;
        }
    }
}
