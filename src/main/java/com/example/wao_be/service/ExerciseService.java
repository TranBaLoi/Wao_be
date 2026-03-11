package com.example.wao_be.service;

import com.example.wao_be.dto.ExerciseDto;
import com.example.wao_be.entity.Exercise;
import com.example.wao_be.entity.ExerciseCategory;
import com.example.wao_be.repository.ExerciseCategoryRepository;
import com.example.wao_be.repository.ExerciseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseCategoryRepository categoryRepository;

    public ExerciseDto.Response create(ExerciseDto.Request req) {
        ExerciseCategory category = null;
        if (req.getCategoryId() != null) {
            category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found: " + req.getCategoryId()));
        }
        Exercise exercise = Exercise.builder()
                .name(req.getName())
                .category(category)
                .videoUrl(req.getVideoUrl())
                .caloriesPerMin(req.getCaloriesPerMin())
                .description(req.getDescription())
                .build();
        return toResponse(exerciseRepository.save(exercise));
    }

    @Transactional(readOnly = true)
    public List<ExerciseDto.Response> search(String name) {
        if (name == null || name.isBlank()) {
            return exerciseRepository.findAll().stream().map(this::toResponse).toList();
        }
        return exerciseRepository.findByNameContainingIgnoreCase(name)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ExerciseDto.Response> getByCategory(Long categoryId) {
        return exerciseRepository.findByCategoryId(categoryId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ExerciseDto.Response getById(Long id) {
        return toResponse(findById(id));
    }

    public Exercise findById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Exercise not found: " + id));
    }

    public void delete(Long id) {
        exerciseRepository.deleteById(id);
    }

    private ExerciseDto.Response toResponse(Exercise e) {
        ExerciseDto.Response r = new ExerciseDto.Response();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setVideoUrl(e.getVideoUrl());
        r.setCaloriesPerMin(e.getCaloriesPerMin());
        r.setDescription(e.getDescription());
        if (e.getCategory() != null) {
            r.setCategoryId(e.getCategory().getId());
            r.setCategoryName(e.getCategory().getName());
        }
        return r;
    }
}

