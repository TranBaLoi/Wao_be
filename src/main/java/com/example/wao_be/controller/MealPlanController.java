package com.example.wao_be.controller;

import com.example.wao_be.dto.MealPlanDto;
import com.example.wao_be.service.MealPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meal-plans")
@RequiredArgsConstructor
public class MealPlanController {

    private final MealPlanService mealPlanService;

    /**
     * POST /api/meal-plans
     * Tạo meal plan (system hoặc user custom)
     * Body: { type: "USER_CUSTOM", userId: 1, name: "...", foods: [...] }
     */
    @PostMapping
    public ResponseEntity<MealPlanDto.Response> create(@Valid @RequestBody MealPlanDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mealPlanService.create(req));
    }

    /**
     * GET /api/meal-plans
     * Lấy tất cả meal plan
     */
    @GetMapping
    public ResponseEntity<List<MealPlanDto.Response>> getAll() {
        return ResponseEntity.ok(mealPlanService.getAll());
    }

    /**
     * GET /api/meal-plans/system
     * Lấy danh sách meal plan do hệ thống gợi ý
     */
    @GetMapping("/system")
    public ResponseEntity<List<MealPlanDto.Response>> getSystemPlans() {
        return ResponseEntity.ok(mealPlanService.getSystemPlans());
    }

    /**
     * GET /api/meal-plans/user/{userId}
     * Lấy meal plan riêng của user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MealPlanDto.Response>> getUserPlans(@PathVariable Long userId) {
        return ResponseEntity.ok(mealPlanService.getUserPlans(userId));
    }

    /**
     * GET /api/meal-plans/{id}
     * Lấy chi tiết 1 meal plan kèm danh sách món ăn
     */
    @GetMapping("/{id}")
    public ResponseEntity<MealPlanDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mealPlanService.getById(id));
    }

    /**
     * DELETE /api/meal-plans/{id}
     * Xóa meal plan
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mealPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

