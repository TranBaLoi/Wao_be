package com.example.wao_be.service;

import com.example.wao_be.dto.MealPlanDto;
import com.example.wao_be.entity.Food;
import com.example.wao_be.entity.MealPlan;
import com.example.wao_be.entity.MealPlanFood;
import com.example.wao_be.entity.User;
import com.example.wao_be.repository.MealPlanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final UserService userService;
    private final FoodService foodService;

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

