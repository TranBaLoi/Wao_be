package com.example.wao_be.service;

import com.example.wao_be.dto.FoodDto;
import com.example.wao_be.entity.Food;
import com.example.wao_be.repository.FoodRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FoodService {

    private final FoodRepository foodRepository;

    public FoodDto.Response create(FoodDto.Request req, boolean isVerified) {
        Food food = Food.builder()
                .name(req.getName())
                .servingSize(req.getServingSize())
                .calories(req.getCalories())
                .protein(req.getProtein())
                .carbs(req.getCarbs())
                .fat(req.getFat())
                .isVerified(isVerified)
                .build();
        return toResponse(foodRepository.save(food));
    }

    @Transactional(readOnly = true)
    public List<FoodDto.Response> search(String name) {
        if (name == null || name.isBlank()) {
            return foodRepository.findAll().stream().map(this::toResponse).toList();
        }
        return foodRepository.findByNameContainingIgnoreCase(name)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FoodDto.Response getById(Long id) {
        return toResponse(findById(id));
    }

    public FoodDto.Response update(Long id, FoodDto.Request req) {
        Food food = findById(id);
        food.setName(req.getName());
        food.setServingSize(req.getServingSize());
        food.setCalories(req.getCalories());
        food.setProtein(req.getProtein());
        food.setCarbs(req.getCarbs());
        food.setFat(req.getFat());
        return toResponse(foodRepository.save(food));
    }

    public void delete(Long id) {
        foodRepository.deleteById(id);
    }

    public Food findById(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Food not found: " + id));
    }

    private FoodDto.Response toResponse(Food f) {
        FoodDto.Response r = new FoodDto.Response();
        r.setId(f.getId());
        r.setName(f.getName());
        r.setServingSize(f.getServingSize());
        r.setCalories(f.getCalories());
        r.setProtein(f.getProtein());
        r.setCarbs(f.getCarbs());
        r.setFat(f.getFat());
        r.setIsVerified(f.getIsVerified());
        return r;
    }
}

