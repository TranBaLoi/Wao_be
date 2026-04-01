// phan cua nam
package com.example.wao_be.service;

import com.example.wao_be.dto.StatisticsDto;
import com.example.wao_be.entity.UserFoodLog;
import com.example.wao_be.entity.UserHealthProfile;
import com.example.wao_be.entity.WeightLog;
import com.example.wao_be.repository.UserFoodLogRepository;
import com.example.wao_be.repository.UserHealthProfileRepository;
import com.example.wao_be.repository.WeightLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
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
    // namthem
    private final UserHealthProfileRepository userHealthProfileRepository;
    private final UserService userService;

    // tổng hợp toàn bộ dinh dưỡng trong 1 ngày
    public StatisticsDto.DailyNutritionResponse getDailyNutrition(Long userId, LocalDate date) {
        userService.findById(userId);

        NutritionAccumulator accumulator = new NutritionAccumulator();
        // add tất cả log lại vào accymulator
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

    // thốn kê dinh dưỡng cho nhiều ngày, có thể group theo ngày, tuần, tháng
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

    // cộng dinh dưỡng từng món ăn 1
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

    public StatisticsDto.WeightSeriesResponse getWeightSeries(
            Long userId,
            LocalDate from,
            LocalDate to,
            StatisticsDto.GroupBy groupBy) {
        userService.findById(userId);
        validateRange(from, to);

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atStartOfDay()
                .plusHours(23)
                .plusMinutes(59)
                .plusSeconds(59);

        // nam them: can nang luon tra ve theo tung ngay, lay newWeight moi nhat cua
        // ngay do
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
        System.out.println("response: " + response);
        return response;
    }

    // thêm phần này để lấy cân nặng gần nhất của user, có thể là từ health profile
    // hoặc log
    public StatisticsDto.LatestWeightInfoResponse getLatestWeightInfo(Long userId) {
        userService.findById(userId);
        UserHealthProfile latestProfile = userHealthProfileRepository
                .findFirstByUserIdOrderByRecordedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("Health profile not found for user: " + userId));

        LatestKnownWeightInfo latestKnown = resolveLatestKnownWeightInfo(userId, latestProfile);

        StatisticsDto.LatestWeightInfoResponse response = new StatisticsDto.LatestWeightInfoResponse();
        response.setUserId(userId);
        response.setLatestKnownWeight(latestKnown.weight());
        response.setLatestKnownDate(latestKnown.date());
        response.setSource(latestKnown.source());
        return response;
    }

    // tính cân nặng gần nhất
    private record LatestKnownWeightInfo(
            Double weight,
            LocalDate date,
            String source) {
    }

    // namthem
    @Transactional
    public StatisticsDto.WeightLogUpdateResponse createWeightLog(
            Long userId,
            StatisticsDto.CreateWeightLogRequest request) {
        System.out.println("nhận req: " + request);
        if (request == null || request.getDate() == null || request.getNewWeight() == null) {
            throw new IllegalArgumentException("date and newWeight are required");
        }
        if (request.getNewWeight() <= 0) {
            throw new IllegalArgumentException("newWeight must be greater than 0");
        }
        // namthem
        if (!LocalDate.now().equals(request.getDate())) {
            throw new IllegalArgumentException("Chỉ được cập nhật cân nặng cho ngày hiện tại.");
        }

        var user = userService.findById(userId);
        UserHealthProfile latestProfile = userHealthProfileRepository
                .findFirstByUserIdOrderByRecordedAtDesc(userId)
                .orElseThrow(() -> new IllegalArgumentException("Health profile not found for user: " + userId));

        LatestKnownWeightInfo latestKnown = resolveLatestKnownWeightInfo(userId, latestProfile);
        validateWeightThreshold(request.getNewWeight(), latestKnown);

        LocalDateTime loggedAt = request.getDate().atStartOfDay();
        // namthem
        if (weightLogRepository.existsByUserIdAndLoggedAtBetween(
                userId,
                loggedAt,
                request.getDate().plusDays(1).atStartOfDay().minusNanos(1))) {
            throw new IllegalArgumentException("Hôm nay đã có bản ghi cân nặng rồi, không thể thêm lần nữa.");
        }
        Double oldWeight = weightLogRepository
                .findFirstByUserIdAndLoggedAtLessThanEqualOrderByLoggedAtDesc(userId, loggedAt)
                .map(WeightLog::getNewWeight)
                .orElse(latestProfile.getWeightKg());

        if (oldWeight == null) {
            oldWeight = request.getNewWeight();
        }

        WeightLog weightLog = WeightLog.builder()
                .user(user)
                .oldWeight(oldWeight)
                .newWeight(request.getNewWeight())
                .note(request.getNote())
                .loggedAt(loggedAt)
                .build();

        WeightLog savedWeightLog = weightLogRepository.save(weightLog);

        try {
            // namthem
            latestProfile.setWeightKg(request.getNewWeight());
            userHealthProfileRepository.save(latestProfile);
        } catch (IllegalArgumentException ex) {
            // namthem
            throw new IllegalArgumentException(
                    "Da ghi log can nang, nhung khong dong bo duoc ho so suc khoe: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            // namthem
            throw new IllegalArgumentException(
                    "Da ghi log can nang, nhung cap nhat ho so suc khoe that bai. Vui long kiem tra muc tieu can nang hien tai.",
                    ex);
        }

        StatisticsDto.WeightLogUpdateResponse response = new StatisticsDto.WeightLogUpdateResponse();
        response.setLogId(savedWeightLog.getId());
        response.setUserId(userId);
        response.setDate(request.getDate());
        response.setOldWeight(savedWeightLog.getOldWeight());
        response.setNewWeight(savedWeightLog.getNewWeight());
        response.setChangeAmount(savedWeightLog.getChangeAmount());
        response.setCurrentProfileWeight(latestProfile.getWeightKg());
        response.setNote(savedWeightLog.getNote());
        response.setLatestKnownWeight(latestKnown.weight());
        response.setLatestKnownDate(latestKnown.date());
        return response;
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

    // namthem
    private LatestKnownWeightInfo resolveLatestKnownWeightInfo(Long userId, UserHealthProfile latestProfile) {
        return weightLogRepository.findFirstByUserIdOrderByLoggedAtDesc(userId)
                .map(log -> new LatestKnownWeightInfo(
                        log.getNewWeight(),
                        log.getLoggedAt() != null ? log.getLoggedAt().toLocalDate() : null,
                        "WEIGHT_LOG"))
                .orElseGet(() -> new LatestKnownWeightInfo(
                        latestProfile.getWeightKg(),
                        latestProfile.getRecordedAt() != null ? latestProfile.getRecordedAt().toLocalDate() : null,
                        "HEALTH_PROFILE"));
    }

    // namthem
    private void validateWeightThreshold(Double newWeight, LatestKnownWeightInfo latestKnown) {
        if (newWeight == null || latestKnown.weight() == null) {
            return;
        }

        double delta = Math.abs(newWeight - latestKnown.weight());
        double allowedDelta = resolveAllowedWeightDelta(latestKnown.date());

        if (delta > allowedDelta) {
            String latestDateText = latestKnown.date() != null ? latestKnown.date().toString() : "khong ro ngay";
            throw new IllegalArgumentException(
                    String.format(
                            "Can nang gan nhat la %.1f kg vao ngay %s. Muc nhap moi %.1f kg vuot nguong cho phep %.1f kg, vui long kiem tra lai.",
                            latestKnown.weight(),
                            latestDateText,
                            newWeight,
                            allowedDelta));
        }
    }

    // thêm ngưỡng
    private double resolveAllowedWeightDelta(LocalDate latestKnownDate) {
        if (latestKnownDate == null) {
            return 8.0;
        }

        long daysSinceLastLog = Math.max(0,
                Duration.between(latestKnownDate.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays());
        if (daysSinceLastLog <= 30) {
            return 5.0;
        }
        if (daysSinceLastLog <= 90) {
            return 8.0;
        }
        if (daysSinceLastLog <= 180) {
            return 12.0;
        }
        return 18.0;
    }

    // tính sự chênh lệnh cân nặng theo ngày, có thể có nhiều log trong ngày, lấy
    // log đầu tiên làm startWeight, log cuối cùng làm endWeight
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
