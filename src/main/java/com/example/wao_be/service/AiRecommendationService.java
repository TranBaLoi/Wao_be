package com.example.wao_be.service;

import com.example.wao_be.entity.Food;
import com.example.wao_be.entity.UserFoodLog;
import com.example.wao_be.entity.UserHealthProfile;
import com.example.wao_be.repository.FoodRepository;
import com.example.wao_be.util.VectorUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiRecommendationService {

    private final FoodRepository foodRepository;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MealPlanFoodDto {
        private Food food;
        private UserFoodLog.MealType mealType;
        private double servingQty;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecommendationResultDto {
        private List<MealPlanFoodDto> foods;
    }

    public RecommendationResultDto recommendMealsForDay(UserHealthProfile profile) {
        if (profile.getTargetCalories() == null) {
            throw new IllegalArgumentException("User profile missing target calories");
        }

        double totalCalo = profile.getTargetCalories();
        double breakfastCalo = totalCalo * 0.25;
        double lunchCalo = totalCalo * 0.40;
        double dinnerCalo = totalCalo * 0.35;

        double[] userVector = VectorUtils.parseVector(profile.getPreferenceVector());

        List<MealPlanFoodDto> allFoods = new ArrayList<>();

        List<Food> breakfastCombo = getMealCombo(breakfastCalo, "BREAKFAST", userVector, profile.getAllergies());
        for (Food f : breakfastCombo) {
            allFoods.add(new MealPlanFoodDto(f, UserFoodLog.MealType.BREAKFAST, 1.0));
        }

        List<Food> lunchCombo = getMealCombo(lunchCalo, "LUNCH", userVector, profile.getAllergies());
        for (Food f : lunchCombo) {
            allFoods.add(new MealPlanFoodDto(f, UserFoodLog.MealType.LUNCH, 1.0));
        }

        List<Food> dinnerCombo = getMealCombo(dinnerCalo, "DINNER", userVector, profile.getAllergies());
        for (Food f : dinnerCombo) {
            allFoods.add(new MealPlanFoodDto(f, UserFoodLog.MealType.DINNER, 1.0));
        }

        return new RecommendationResultDto(allFoods);
    }

    private boolean isSafeToEat(String userAllergies, String foodAllergens) {
        if (userAllergies == null || userAllergies.trim().isEmpty() ||
            foodAllergens == null || foodAllergens.trim().isEmpty()) {
            return true;
        }
        Set<String> uAllergies = Arrays.stream(userAllergies.split(","))
                                       .map(String::trim)
                                       .map(String::toUpperCase)
                                       .collect(Collectors.toSet());
        Set<String> fAllergens = Arrays.stream(foodAllergens.split(","))
                                       .map(String::trim)
                                       .map(String::toUpperCase)
                                       .collect(Collectors.toSet());
        return Collections.disjoint(uAllergies, fAllergens);
    }

    private List<Food> getMealCombo(double targetCalo, String mealType, double[] userVector, String userAllergies) {
        List<Food> candidates = foodRepository.findBySuitableMealTypesContaining(mealType);

        if (candidates == null || candidates.isEmpty()) {
            throw new RuntimeException("Cannot find any suitable candidate food for " + mealType);
        }

        candidates = candidates.stream()
                .filter(food -> isSafeToEat(userAllergies, food.getContainsAllergens()))
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            throw new RuntimeException("Cannot find any safe candidate food for " + mealType + " after allergy filtering");
        }

        candidates.sort((f1, f2) -> {
            double s1 = VectorUtils.cosineSimilarity(userVector, VectorUtils.parseVector(f1.getFeatureVector()));
            double s2 = VectorUtils.cosineSimilarity(userVector, VectorUtils.parseVector(f2.getFeatureVector()));
            return Double.compare(s2, s1);
        });

        List<Food> combo = new ArrayList<>();
        double currentCalo = 0.0;

        for (Food food : candidates) {
            if (currentCalo + food.getCalories() <= targetCalo + 100) {
                combo.add(food);
                currentCalo += food.getCalories();
            }
            if (Math.abs(targetCalo - currentCalo) <= 150) {
                break;
            }
        }

        if (combo.isEmpty()) {
            combo.add(candidates.get(0));
        }

        return combo;
    }
}
