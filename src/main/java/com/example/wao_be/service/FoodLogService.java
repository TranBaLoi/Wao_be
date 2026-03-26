package com.example.wao_be.service;

import com.example.wao_be.dto.FoodLogDto;
import com.example.wao_be.entity.Food;
import com.example.wao_be.entity.User;
import com.example.wao_be.entity.UserFoodLog;
import com.example.wao_be.entity.UserHealthProfile;
import com.example.wao_be.repository.UserFoodLogRepository;
import com.example.wao_be.repository.UserHealthProfileRepository;
import com.example.wao_be.util.VectorUtils;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FoodLogService {

    private final UserFoodLogRepository foodLogRepository;
    private final UserService userService;
    private final FoodService foodService;
    private final UserHealthProfileRepository userHealthProfileRepository;

    public FoodLogDto.Response log(Long userId, FoodLogDto.Request req) {
        User user = userService.findById(userId);
        Food food = foodService.findById(req.getFoodId());

        UserFoodLog log = UserFoodLog.builder()
                .user(user)
                .food(food)
                .mealType(req.getMealType())
                .servingQty(req.getServingQty())
                .totalCalories(0.0) // will be computed by @PrePersist
                .logDate(req.getLogDate())
                .build();

        UserFoodLog savedLog = foodLogRepository.save(log);
        learnUserPreference(userId, food);

        return toResponse(savedLog);
    }

    private void learnUserPreference(Long userId, Food food) {
        if (food.getFeatureVector() == null || food.getFeatureVector().trim().isEmpty()) {
            return; // No feature vector to learn from
        }

        userHealthProfileRepository.findFirstByUserIdOrderByRecordedAtDesc(userId)
                .ifPresent(profile -> {
                    if (profile.getPreferenceVector() != null && !profile.getPreferenceVector().trim().isEmpty()) {
                        double[] userPrefs = VectorUtils.parseVector(profile.getPreferenceVector());
                        double[] foodFeatures = VectorUtils.parseVector(food.getFeatureVector());

                        double[] newPrefs = VectorUtils.updatePreferenceVector(userPrefs, foodFeatures, 0.8);
                        profile.setPreferenceVector(VectorUtils.formatVector(newPrefs));

                        userHealthProfileRepository.save(profile);
                    } else {
                        // Initialize preference vector with food features if it's currently empty
                        profile.setPreferenceVector(food.getFeatureVector());
                        userHealthProfileRepository.save(profile);
                    }
                });
    }

    @Transactional(readOnly = true)
    public List<FoodLogDto.Response> getByUserAndDate(Long userId, LocalDate date) {
        return foodLogRepository.findByUserIdAndLogDate(userId, date)
                .stream().map(this::toResponse).toList();
    }

    public void delete(Long logId) {
        if (!foodLogRepository.existsById(logId)) {
            throw new EntityNotFoundException("FoodLog not found: " + logId);
        }
        foodLogRepository.deleteById(logId);
    }

    public List<FoodLogDto.Response> getByMealType(UserFoodLog.MealType mealType) {
        return foodLogRepository.findByMealType(mealType)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FoodLogDto.Response> getByUserDateAndMealType(
            Long userId,
            LocalDate date,
            UserFoodLog.MealType mealType
    ){
        return foodLogRepository.findByUserIdAndLogDateAndMealType(userId, date, mealType)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    private FoodLogDto.Response toResponse(UserFoodLog l) {
        FoodLogDto.Response r = new FoodLogDto.Response();
        r.setId(l.getId());
        r.setUserId(l.getUser().getId());
        r.setFoodId(l.getFood().getId());
        r.setFoodName(l.getFood().getName());
        r.setMealType(l.getMealType());
        r.setServingQty(l.getServingQty());
        r.setTotalCalories(l.getTotalCalories());
        r.setLogDate(l.getLogDate());
        return r;
    }

}

