package com.example.wao_be.controller;

import com.example.wao_be.dto.FoodDto;
import com.example.wao_be.dto.FoodImageDto;
import com.example.wao_be.service.FoodImageService;
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
    private final FoodImageService foodImageService;

    /** POST /api/foods  (user tự tạo, isVerified=false) */
    @PostMapping
    public ResponseEntity<FoodDto.Response> create(@Valid @RequestBody FoodDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foodService.create(req, false));
    }

    /** POST /api/foods/form (user tạo món + upload ảnh) */
    @PostMapping(path = "/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodDto.Response> createFromForm(@Valid @ModelAttribute FoodDto.FormRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foodService.createFromForm(req, false));
    }

    /** POST /api/foods/admin  (admin tạo, isVerified=true) */
    @PostMapping("/admin")
    public ResponseEntity<FoodDto.Response> createVerified(@Valid @RequestBody FoodDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foodService.create(req, true));
    }

    /** POST /api/foods/admin/form (admin tạo món + upload ảnh) */
    @PostMapping(path = "/admin/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodDto.Response> createVerifiedFromForm(@Valid @ModelAttribute FoodDto.FormRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foodService.createFromForm(req, true));
    }

    /** GET /api/foods/search?name= */
    @GetMapping("/search")
    public ResponseEntity<List<FoodDto.Response>> search(
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(foodService.search(name));
    }

    /** GET /api/foods/{id} */
    @GetMapping("/search/{id}")
    public ResponseEntity<FoodDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getById(id));
    }

    /** PUT /api/foods/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<FoodDto.Response> update(@PathVariable Long id,
                                                   @Valid @RequestBody FoodDto.Request req) {
        return ResponseEntity.ok(foodService.update(id, req));
    }

    /** PUT /api/foods/{id}/form (update món + upload thêm ảnh mới) */
    @PutMapping(path = "/{id}/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodDto.Response> updateFromForm(@PathVariable Long id,
                                                           @Valid @ModelAttribute FoodDto.FormRequest req) {
        return ResponseEntity.ok(foodService.updateFromForm(id, req));
    }

    /** DELETE /api/foods/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        foodService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** POST /api/foods/{foodId}/images (thêm 1 ảnh cho món ăn) */
    @PostMapping(path = "/{foodId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodImageDto.Response> addImage(
            @PathVariable Long foodId,
            @RequestPart("image") MultipartFile image
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(foodImageService.create(foodId, image));
    }

    /** GET /api/foods/{foodId}/images */
    @GetMapping("/{foodId}/images")
    public ResponseEntity<List<FoodImageDto.Response>> getImagesByFoodId(@PathVariable Long foodId) {
        return ResponseEntity.ok(foodImageService.getByFoodId(foodId));
    }

    /** GET /api/foods/images/{imageId} */
    @GetMapping("/images/{imageId}")
    public ResponseEntity<FoodImageDto.Response> getImageById(@PathVariable Long imageId) {
        return ResponseEntity.ok(foodImageService.getById(imageId));
    }

    /** PUT /api/foods/images/{imageId} */
    @PutMapping(path = "/images/{imageId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodImageDto.Response> updateImage(
            @PathVariable Long imageId,
            @RequestPart("image") MultipartFile image
    ) {
        return ResponseEntity.ok(foodImageService.update(imageId, image));
    }

    /** DELETE /api/foods/images/{imageId} */
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        foodImageService.delete(imageId);
        return ResponseEntity.noContent().build();
    }
}

