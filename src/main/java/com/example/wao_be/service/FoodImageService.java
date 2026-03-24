package com.example.wao_be.service;

import com.example.wao_be.dto.FoodImageDto;
import com.example.wao_be.entity.Food;
import com.example.wao_be.entity.FoodImage;
import com.example.wao_be.repository.FoodImageRepository;
import com.example.wao_be.repository.FoodRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FoodImageService {

    private final FoodImageRepository foodImageRepository;
    private final FoodRepository foodRepository;

    public FoodImageDto.Response create(Long foodId, MultipartFile image) {
        Food food = findFood(foodId);
        FoodImage foodImage = toEntity(image);
        foodImage.setFood(food);
        return toResponse(foodImageRepository.save(foodImage));
    }

    public List<FoodImageDto.Response> addMany(Long foodId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        Food food = findFood(foodId);
        return images.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> {
                    FoodImage foodImage = toEntity(file);
                    foodImage.setFood(food);
                    return toResponse(foodImageRepository.save(foodImage));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FoodImageDto.Response> getByFoodId(Long foodId) {
        if (!foodRepository.existsById(foodId)) {
            throw new EntityNotFoundException("Food not found: " + foodId);
        }
        return foodImageRepository.findByFoodId(foodId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FoodImageDto.Response getById(Long id) {
        return toResponse(findImage(id));
    }

    public FoodImageDto.Response update(Long id, MultipartFile image) {
        FoodImage foodImage = findImage(id);
        validateImage(image);
        try {
            foodImage.setFileName(resolveFileName(image));
            foodImage.setContentType(image.getContentType());
            foodImage.setFileSize(image.getSize());
            foodImage.setData(image.getBytes());
        } catch (IOException ex) {
            throw new IllegalArgumentException("Cannot read image bytes", ex);
        }
        return toResponse(foodImageRepository.save(foodImage));
    }

    public void delete(Long id) {
        if (!foodImageRepository.existsById(id)) {
            throw new EntityNotFoundException("FoodImage not found: " + id);
        }
        foodImageRepository.deleteById(id);
    }

    private FoodImage findImage(Long id) {
        return foodImageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FoodImage not found: " + id));
    }

    private Food findFood(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Food not found: " + id));
    }

    private FoodImage toEntity(MultipartFile image) {
        validateImage(image);
        try {
            return FoodImage.builder()
                    .fileName(resolveFileName(image))
                    .contentType(image.getContentType())
                    .fileSize(image.getSize())
                    .data(image.getBytes())
                    .build();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Cannot read image bytes", ex);
        }
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be empty");
        }
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }
    }

    private String resolveFileName(MultipartFile image) {
        String originalFileName = image.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            return "image";
        }
        return originalFileName;
    }

    private FoodImageDto.Response toResponse(FoodImage image) {
        FoodImageDto.Response response = new FoodImageDto.Response();
        response.setId(image.getId());
        response.setFoodId(image.getFood().getId());
        response.setFileName(image.getFileName());
        response.setContentType(image.getContentType());
        response.setFileSize(image.getFileSize());
        response.setData(image.getData());
        response.setCreatedAt(image.getCreatedAt());
        response.setUpdatedAt(image.getUpdatedAt());
        return response;
    }
}

