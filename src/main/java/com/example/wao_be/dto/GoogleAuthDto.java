package com.example.wao_be.dto;

import lombok.Data;

public class GoogleAuthDto {

    @Data
    public static class GoogleLoginRequest {
        // The ID token received from Google on the client
        private String idToken;
    }

    @Data
    public static class VerifyEmailRequest {
        // The ID token received from Google on the client
        private String idToken;
    }

    @Data
    public static class VerifyEmailResponse {
        private String email;
        private boolean verified;
    }
}

