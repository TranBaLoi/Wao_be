package com.example.wao_be.service;

import com.example.wao_be.dto.MealPlanDto;
import com.example.wao_be.entity.*;
import com.example.wao_be.repository.MealPlanRepository;
import com.example.wao_be.repository.UserFoodLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final UserService userService;
    private final FoodService foodService;

    private final UserFoodLogRepository userFoodLogRepository;
    private final DailySummaryService dailySummaryService;

    /** Tạo meal plan (system hoặc user custom) */
    public MealPlanDto.Response create(MealPlanDto.Request req) {
        User user = null;
        if (req.getType() == MealPlan.MealPlanType.USER_CUSTOM) {
            if (req.getUserId() == null) {
                throw new IllegalArgumentException("userId is required for USER_CUSTOM meal plan.");
            }
            user = userService.findById(req.getUserId());
        }

        MealPlan mealPlan = MealPlan.builder()
                .name(req.getName())
                .description(req.getDescription())
                .type(req.getType())
                .user(user)
                .mealPlanFoods(new ArrayList<>())
                .build();

        // Thêm các món ăn
        if (req.getFoods() != null) {
            for (MealPlanDto.Request.FoodItem item : req.getFoods()) {
                Food food = foodService.findById(item.getFoodId());
                MealPlanFood mpf = MealPlanFood.builder()
                        .mealPlan(mealPlan)
                        .food(food)
                        .mealType(item.getMealType())
                        .servingQty(item.getServingQty())
                        .build();
                mealPlan.getMealPlanFoods().add(mpf);
            }
        }

        mealPlan.recalculateTotalCalories();
        return toResponse(mealPlanRepository.save(mealPlan));
    }

    /** Lấy tất cả meal plan hệ thống (SYSTEM_SUGGESTION) */
    @Transactional(readOnly = true)
    public List<MealPlanDto.Response> getSystemPlans() {
        return mealPlanRepository.findByType(MealPlan.MealPlanType.SYSTEM_SUGGESTION)
                .stream().map(this::toResponse).toList();
    }

    /** Lấy meal plan của một user cụ thể (USER_CUSTOM) */
    @Transactional(readOnly = true)
    public List<MealPlanDto.Response> getUserPlans(Long userId) {
        return mealPlanRepository.findByUserId(userId)
                .stream().map(this::toResponse).toList();
    }

    /** Lấy tất cả (system + user custom) */
    @Transactional(readOnly = true)
    public List<MealPlanDto.Response> getAll() {
        return mealPlanRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    /** Lấy chi tiết 1 meal plan */
    @Transactional(readOnly = true)
    public MealPlanDto.Response getById(Long id) {
        return toResponse(findById(id));
    }

    /** Xóa meal plan */
    public void delete(Long id) {
        if (!mealPlanRepository.existsById(id)) {
            throw new EntityNotFoundException("MealPlan not found: " + id);
        }
        mealPlanRepository.deleteById(id);
    }

    public MealPlan findById(Long id) {
        return mealPlanRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MealPlan not found: " + id));
    }

    public MealPlanDto.ApplyResponse applyToDate(Long mealPlanId, MealPlanDto.ApplyRequest req) {
        MealPlan mealPlan = findById(mealPlanId);
        User user = userService.findById(req.getUserId());
        LocalDate logDate = req.getLogDate();

        if (mealPlan.getType() == MealPlan.MealPlanType.USER_CUSTOM) {
            if (mealPlan.getUser() == null || !mealPlan.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("You can only apply your own USER_CUSTOM meal plan.");
            }
        }

        int previousCount = userFoodLogRepository.findByUserIdAndLogDate(user.getId(), logDate).size();

        userFoodLogRepository.deleteByUserIdAndLogDate(user.getId(), logDate);

        if (mealPlan.getMealPlanFoods() == null || mealPlan.getMealPlanFoods().isEmpty()) {
            throw new IllegalArgumentException("Meal plan has no food items to apply.");
        }

        var logs = mealPlan.getMealPlanFoods().stream()
                .map(item -> UserFoodLog.builder()
                        .user(user)
                        .food(item.getFood())
                        .mealType(item.getMealType())
                        .servingQty(item.getServingQty())
                        .totalCalories(0.0)
                        .logDate(logDate)
                        .build())
                .toList();

        userFoodLogRepository.saveAll(logs);
        dailySummaryService.buildAndSave(user.getId(), logDate);

        MealPlanDto.ApplyResponse res = new MealPlanDto.ApplyResponse();
        res.setMealPlanId(mealPlanId);
        res.setUserId(user.getId());
        res.setLogDate(logDate);
        res.setPreviousItems(previousCount);
        res.setAddedItems(logs.size());
        res.setMessage("Applied meal plan successfully.");
        return res;
    }



    // ---- Mapper ----
    private MealPlanDto.Response toResponse(MealPlan mp) {
        MealPlanDto.Response r = new MealPlanDto.Response();
        r.setId(mp.getId());
        r.setName(mp.getName());
        r.setDescription(mp.getDescription());
        r.setTotalCalories(mp.getTotalCalories());
        r.setType(mp.getType());

        if (mp.getUser() != null) {
            r.setUserId(mp.getUser().getId());
            r.setUserName(mp.getUser().getFullName());
        }

        if (mp.getMealPlanFoods() != null) {
            r.setFoods(mp.getMealPlanFoods().stream().map(mpf -> {
                MealPlanDto.Response.FoodItemResponse fi = new MealPlanDto.Response.FoodItemResponse();
                fi.setId(mpf.getId());
                fi.setFoodId(mpf.getFood().getId());
                fi.setFoodName(mpf.getFood().getName());
                fi.setMealType(mpf.getMealType());
                fi.setServingQty(mpf.getServingQty());
                fi.setCalories(mpf.getCalories());
                return fi;
            }).toList());
        }
        return r;
    }
}

