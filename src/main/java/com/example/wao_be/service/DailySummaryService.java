package com.example.wao_be.service;

import com.example.wao_be.dto.DailySummaryDto;
import com.example.wao_be.entity.DailySummary;
import com.example.wao_be.entity.User;
import com.example.wao_be.entity.UserHealthProfile;
import com.example.wao_be.repository.DailySummaryRepository;
import com.example.wao_be.repository.UserFoodLogRepository;
import com.example.wao_be.repository.UserWaterLogRepository;
import com.example.wao_be.repository.UserWorkoutLogRepository;
import com.example.wao_be.repository.StepLogRepository;
import com.example.wao_be.repository.UserHealthProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DailySummaryService {

    private final DailySummaryRepository dailySummaryRepository;
    private final UserFoodLogRepository foodLogRepository;
    private final UserWorkoutLogRepository workoutLogRepository;
    private final UserWaterLogRepository waterLogRepository;
    private final StepLogRepository stepLogRepository;
    private final UserHealthProfileRepository healthProfileRepository;
    private final UserService userService;

    /**
     * Tổng hợp dữ liệu ngày và lưu/cập nhật DailySummary.
     * Gọi sau mỗi lần user thêm food log, workout log, water log, step log.
     */
    public DailySummaryDto buildAndSave(Long userId, LocalDate date) {
        User user = userService.findById(userId);

        double calIn  = foodLogRepository.sumCaloriesByUserIdAndLogDate(userId, date);
        double calOut = workoutLogRepository.sumCaloriesBurnedByUserIdAndLogDate(userId, date);
        int water     = waterLogRepository.sumWaterByUserIdAndLogDate(userId, date);
        int steps     = stepLogRepository.sumStepsByUserIdAndLogDate(userId, date);

        UserHealthProfile latestProfile = healthProfileRepository
                .findFirstByUserIdOrderByRecordedAtDesc(userId)
                .orElse(null);

        // targetCalories lúc này là TDEE +/- dailyCalories (Tổng calo cần ăn 1 ngày)
        double targetCalories = latestProfile != null && latestProfile.getTargetCalories() != null
                ? latestProfile.getTargetCalories()
                : 2000.0;

        // Số calo net = lượng nạp vào - lượng tiêu hao phụ thêm (để so sánh với mục tiêu theo TDEE)
        // Lưu ý: TDEE đã bao gồm calOut cơ bản. Ở đây nếu calOut là calOut từ tập luyện TAI APP
        // thì mình so sánh tuỳ theo business (thường Net = calIn sẽ so với targetCalories).
        // Giả sử: calIn là tổng ăn, cần đạt đủ targetCalories (đã trừ/cộng).
        double netCalories = calIn; // Tập trung vào calIn để đạt target ăn uống.

        boolean goalAchieved;
        if (latestProfile != null && latestProfile.getGoalType() != null) {
            switch (latestProfile.getGoalType()) {
                // Tăng cân: Phải ăn lớn hơn hoặc bằng target
                case GAIN_WEIGHT -> goalAchieved = netCalories >= targetCalories;
                // Giảm cân: Ăn nhỏ hơn hoặc bằng target (nhưng nên có mức min để an toàn, tạm check <= target)
                case LOSE_WEIGHT -> goalAchieved = netCalories <= targetCalories && netCalories > 0;
                // Duy trì: Dao động +- 100 calo
                case MAINTAIN -> goalAchieved = Math.abs(netCalories - targetCalories) <= 100 && netCalories > 0;
                default -> goalAchieved = false;
            }
        } else {
            goalAchieved = Math.abs(netCalories - targetCalories) <= 100 && netCalories > 0;
        }

        DailySummary summary = dailySummaryRepository
                .findByUserIdAndLogDate(userId, date)
                .orElse(DailySummary.builder().user(user).logDate(date).build());

        summary.setTotalCalIn(calIn);
        summary.setTotalCalOut(calOut);
        summary.setTotalWater(water);
        summary.setTotalSteps(steps);
        summary.setIsGoalAchieved(goalAchieved);

        return toDto(dailySummaryRepository.save(summary));
    }

    @Transactional(readOnly = true)
    public DailySummaryDto getByUserAndDate(Long userId, LocalDate date) {
        return dailySummaryRepository.findByUserIdAndLogDate(userId, date)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No daily summary for user " + userId + " on " + date));
    }

    @Transactional(readOnly = true)
    public List<DailySummaryDto> getHistory(Long userId, LocalDate from, LocalDate to) {
        return dailySummaryRepository
                .findByUserIdAndLogDateBetweenOrderByLogDateAsc(userId, from, to)
                .stream().map(this::toDto).toList();
    }

    private DailySummaryDto toDto(DailySummary s) {
        DailySummaryDto dto = new DailySummaryDto();
        dto.setUserId(s.getUser().getId());
        dto.setLogDate(s.getLogDate());
        dto.setTotalCalIn(s.getTotalCalIn());
        dto.setTotalCalOut(s.getTotalCalOut());
        dto.setNetCalories(s.getTotalCalIn() - s.getTotalCalOut());
        dto.setTotalWater(s.getTotalWater());
        dto.setTotalSteps(s.getTotalSteps());
        dto.setIsGoalAchieved(s.getIsGoalAchieved());
        return dto;
    }
}
