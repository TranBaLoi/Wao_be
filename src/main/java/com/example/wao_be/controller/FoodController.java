package com.example.wao_be.controller;

import com.example.wao_be.dto.FoodDto;
import com.example.wao_be.service.FoodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    /** POST /api/foods  (user tự tạo, gửi form-data, ảnh optional) */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodDto.Response> create(
            @Valid @RequestPart("food") FoodDto.Request req,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foodService.create(req, images, false));
    }

    /** POST /api/foods/admin (admin tạo, gửi form-data, ảnh optional) */
    @PostMapping(path = "/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodDto.Response> createVerifiedFromForm(
            @Valid @RequestPart("food") FoodDto.Request req,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foodService.create(req, images, true));
    }

    /** GET /api/foods/search?name= */
    @GetMapping("/search")
    public ResponseEntity<List<FoodDto.Response>> search(
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(foodService.search(name));
    }

    /** GET /api/foods/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<FoodDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getById(id));
    }

    /** PUT /api/foods/{id} (gửi form-data, upload thêm ảnh mới nếu có) */
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestPart("food") FoodDto.Request req,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return ResponseEntity.ok(foodService.update(id, req, images));
    }


    /** DELETE /api/foods/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        foodService.delete(id);
        return ResponseEntity.noContent().build();
    }


}

