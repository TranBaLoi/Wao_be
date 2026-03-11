package com.example.wao_be.dto;

import com.example.wao_be.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class UserDto {

    @Data
    public static class RegisterRequest {
        @NotBlank @Email
        private String email;

        @NotBlank @Size(min = 6, max = 100)
        private String password;

        @NotBlank
        private String fullName;
    }

    @Data
    public static class UpdateRequest {
        private String fullName;
        private User.UserStatus status;
    }

    @Data
    public static class Response {
        private Long id;
        private String email;
        private String fullName;
        private User.UserStatus status;
    }
}

