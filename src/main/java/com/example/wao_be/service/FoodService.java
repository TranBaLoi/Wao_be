package com.example.wao_be.service;

import com.example.wao_be.dto.FoodDto;
import com.example.wao_be.entity.Food;
import com.example.wao_be.repository.FoodRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FoodService {

    private final FoodRepository foodRepository;
    private final ImageStorageService imageStorageService;

    public FoodDto.Response create(FoodDto.Request req, List<MultipartFile> images, boolean isVerified) {
        Food food = toFood(req, isVerified);
        food.setImageUrls(new ArrayList<>());
        Food savedFood = foodRepository.save(food);

        List<String> uploadedUrls = uploadImages(images);
        if (!uploadedUrls.isEmpty()) {
            savedFood.getImageUrls().addAll(uploadedUrls);
            savedFood = foodRepository.save(savedFood);
        }
        return toResponse(savedFood);
    }

    public FoodDto.Response update(Long id, FoodDto.Request req, List<MultipartFile> images) {
        Food food = findById(id);
        food.setName(req.getName());
        food.setServingSize(req.getServingSize());
        food.setCalories(req.getCalories());
        food.setProtein(req.getProtein());
        food.setCarbs(req.getCarbs());
        food.setFat(req.getFat());

        if (food.getImageUrls() == null) {
            food.setImageUrls(new ArrayList<>());
        }

        // Update form-data se upload them anh moi, khong xoa anh cu.
        List<String> uploadedUrls = uploadImages(images);
        food.getImageUrls().addAll(uploadedUrls);

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
        food.setFeatureVector(req.getFeatureVector());
        food.setSuitableMealTypes(req.getSuitableMealTypes());
        return toResponse(foodRepository.save(food));
    }

    public void delete(Long id) {
        Food food = findById(id);
        List<String> imageUrls = food.getImageUrls() == null
                ? List.of()
                : new ArrayList<>(food.getImageUrls());

        foodRepository.delete(food);

        // Xoa anh tren Cloudinary theo co che best-effort de tranh ton tai orphan file.
        for (String imageUrl : imageUrls) {
            try {
                imageStorageService.deleteImage(imageUrl);
            } catch (IOException ignored) {
                // Bo qua loi xoa remote; ban ghi DB da bi xoa thanh cong.
            }
        }
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
        r.setFeatureVector(f.getFeatureVector());
        r.setSuitableMealTypes(f.getSuitableMealTypes());
        r.setImageUrls(f.getImageUrls() == null ? List.of() : new ArrayList<>(f.getImageUrls()));
        return r;
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        try {
            return imageStorageService.uploadImages(images);
        } catch (IOException e) {
            throw new IllegalStateException("Upload food images failed", e);
        }
    }


    private Food toFood(FoodDto.Request req, boolean isVerified) {
        return Food.builder()
                .name(req.getName())
                .servingSize(req.getServingSize())
                .calories(req.getCalories())
                .protein(req.getProtein())
                .carbs(req.getCarbs())
                .fat(req.getFat())
                .isVerified(isVerified)
                .featureVector(req.getFeatureVector())
                .suitableMealTypes(req.getSuitableMealTypes())
                .build();
    }

    private Food toFood(FoodDto.FormRequest req, boolean isVerified) {
        return Food.builder()
                .name(req.getName())
                .servingSize(req.getServingSize())
                .calories(req.getCalories())
                .protein(req.getProtein())
                .carbs(req.getCarbs())
                .fat(req.getFat())
                .isVerified(isVerified)
                .featureVector(req.getFeatureVector())
                .suitableMealTypes(req.getSuitableMealTypes())
                .imageUrls(new ArrayList<>())
                .build();
    }
}

