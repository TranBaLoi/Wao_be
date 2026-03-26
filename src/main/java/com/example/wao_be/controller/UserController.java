package com.example.wao_be.controller;

import com.example.wao_be.dto.GoogleAuthDto;
import com.example.wao_be.dto.UserDto;
import com.example.wao_be.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** POST /api/users/register */
    @PostMapping("/register")
    public ResponseEntity<UserDto.Response> register(@Valid @RequestBody UserDto.RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(req));
    }

    /** POST /api/users/google-login */
    @PostMapping("/google-login")
    public ResponseEntity<UserDto.Response> loginWithGoogle(@RequestBody GoogleAuthDto.GoogleLoginRequest req) {
        return ResponseEntity.ok(userService.loginWithGoogle(req));
    }

    /** POST /api/users/verify */
    @PostMapping("/verify")
    public ResponseEntity<GoogleAuthDto.VerifyEmailResponse> verifyEmail(@RequestBody GoogleAuthDto.VerifyEmailRequest req) {
        return ResponseEntity.ok(userService.verifyEmail(req));
    }

    /** GET /api/users */
    @GetMapping
    public ResponseEntity<List<UserDto.Response>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    /** GET /api/users/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    /** PUT /api/users/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto.Response> update(@PathVariable Long id,
                                                   @RequestBody UserDto.UpdateRequest req) {
        return ResponseEntity.ok(userService.update(id, req));
    }

    /** DELETE /api/users/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
