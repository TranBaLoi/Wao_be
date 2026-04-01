package com.example.wao_be.controller;

import com.example.wao_be.dto.GoogleAuthDto;
import com.example.wao_be.dto.UserDto;
import com.example.wao_be.service.ImageStorageService;
import com.example.wao_be.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ImageStorageService imageStorageService;

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

    /** PUT /api/users/{id}/password */
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                               @Valid @RequestBody UserDto.ChangePasswordRequest req) {
        userService.changePassword(id, req);
        return ResponseEntity.ok().build();
    }

    /** POST /api/users/upload-avatar
     * Phía BE nhận ảnh, push lên Cloudinary và trả về img_url để Frontend có thể tiếp tục xử lý lưu (ví dụ PUT update user profile)
     */
    @PostMapping(value = "/upload-avatar", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, String>> uploadAvatarOnly(@RequestParam("file") MultipartFile file) throws Exception {
        String avatarUrl = imageStorageService.uploadAvatar(file);
        return ResponseEntity.ok(java.util.Map.of("imgUrl", avatarUrl));
    }

    /** POST /api/users/{id}/avatar */
    @PostMapping(value = "/{id}/avatar", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.Map<String, String>> uploadAvatar(@PathVariable Long id,
                                                         @RequestParam("file") MultipartFile file) throws Exception {
        String avatarUrl = imageStorageService.uploadAvatar(file);
        userService.updateAvatarUrl(id, avatarUrl);
        return ResponseEntity.ok(java.util.Map.of("imgUrl", avatarUrl));
    }

    /** DELETE /api/users/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
