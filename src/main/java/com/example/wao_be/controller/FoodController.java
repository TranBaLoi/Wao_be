package com.example.wao_be.controller;

import com.example.wao_be.dto.FoodDto;
import com.example.wao_be.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    /** POST /api/foods  (user tự tạo, isVerified=false) */
    @PostMapping
    public ResponseEntity<FoodDto.Response> create(@Valid @RequestBody FoodDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foodService.create(req, false));
    }

    /** POST /api/foods/admin  (admin tạo, isVerified=true) */
    @PostMapping("/admin")
    public ResponseEntity<FoodDto.Response> createVerified(@Valid @RequestBody FoodDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foodService.create(req, true));
    }

    /** GET /api/foods?name= */
    @GetMapping
    public ResponseEntity<List<FoodDto.Response>> search(
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(foodService.search(name));
    }

    /** GET /api/foods/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<FoodDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getById(id));
    }

    /** PUT /api/foods/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<FoodDto.Response> update(@PathVariable Long id,
                                                   @Valid @RequestBody FoodDto.Request req) {
        return ResponseEntity.ok(foodService.update(id, req));
    }

    /** DELETE /api/foods/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        foodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

