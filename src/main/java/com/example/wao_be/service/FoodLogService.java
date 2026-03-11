package com.example.wao_be.service;

import com.example.wao_be.dto.FoodLogDto;
import com.example.wao_be.entity.Food;
import com.example.wao_be.entity.User;
import com.example.wao_be.entity.UserFoodLog;
import com.example.wao_be.repository.UserFoodLogRepository;
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
        return toResponse(foodLogRepository.save(log));
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

