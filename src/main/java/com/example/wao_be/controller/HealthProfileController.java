package com.example.wao_be.controller;

import com.example.wao_be.dto.HealthProfileDto;
import com.example.wao_be.service.HealthProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/health-profiles")
@RequiredArgsConstructor
public class HealthProfileController {

    private final HealthProfileService healthProfileService;

    /** POST /api/users/{userId}/health-profiles */
    @PostMapping
    public ResponseEntity<HealthProfileDto.Response> create(
            @PathVariable Long userId,
            @Valid @RequestBody HealthProfileDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(healthProfileService.create(userId, req));
    }

    /** GET /api/users/{userId}/health-profiles/latest */
    @GetMapping("/latest")
    public ResponseEntity<HealthProfileDto.Response> getLatest(@PathVariable Long userId) {
        return ResponseEntity.ok(healthProfileService.getLatest(userId));
    }

    /** GET /api/users/{userId}/health-profiles/history */
    @GetMapping("/history")
    public ResponseEntity<List<HealthProfileDto.Response>> getHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(healthProfileService.getHistory(userId));
    }
}

