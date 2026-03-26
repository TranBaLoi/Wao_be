package com.example.wao_be.service;

import com.example.wao_be.dto.GoogleAuthDto;
import com.example.wao_be.dto.UserDto;
import com.example.wao_be.entity.User;
import com.example.wao_be.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserDto.Response register(UserDto.RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + req.getEmail());
        }
        User user = User.builder()
                .email(req.getEmail())
                // In production, hash the password with BCrypt
                .passwordHash(req.getPassword())
                .fullName(req.getFullName())
                .status(User.UserStatus.ACTIVE)
                .build();
        return toResponse(userRepository.save(user));
    }

    /**
     * Login or register using Google ID token.
     * We verify token by calling Google's tokeninfo endpoint.
     */
    public UserDto.Response loginWithGoogle(GoogleAuthDto.GoogleLoginRequest req) {
        Map<String, Object> tokenInfo = verifyIdTokenWithGoogle(req.getIdToken());
        if (tokenInfo == null) {
            throw new IllegalArgumentException("Invalid Google ID token");
        }
        String email = (String) tokenInfo.get("email");
        String name = (String) tokenInfo.get("name");
        // Google tokeninfo commonly contains 'picture' with avatar URL
        String picture = tokenInfo.get("picture") != null ? (String) tokenInfo.get("picture") : null;
        if (email == null) throw new IllegalArgumentException("No email in token");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            // create new user
            User u = User.builder()
                    .email(email)
                    // random password (not used) - in production, generate a secure random value or mark as oauth user
                    .passwordHash(UUID.randomUUID().toString())
                    .fullName(name)
                    .status(User.UserStatus.ACTIVE)
                    .img(picture)
                    .build();
            return userRepository.save(u);
        });
        // If existing user doesn't have img but Google provides one, update it
        if ((user.getImg() == null || user.getImg().isEmpty()) && picture != null) {
            user.setImg(picture);
            userRepository.save(user);
        }
        return toResponse(user);
    }

    public GoogleAuthDto.VerifyEmailResponse verifyEmail(GoogleAuthDto.VerifyEmailRequest req) {
        Map<String, Object> tokenInfo = verifyIdTokenWithGoogle(req.getIdToken());
        GoogleAuthDto.VerifyEmailResponse res = new GoogleAuthDto.VerifyEmailResponse();
        if (tokenInfo == null) {
            res.setVerified(false);
            res.setEmail(null);
            return res;
        }
        String email = (String) tokenInfo.get("email");
        Boolean emailVerified = false;
        Object ev = tokenInfo.get("email_verified");
        if (ev instanceof Boolean) emailVerified = (Boolean) ev;
        else if (ev instanceof String) emailVerified = Boolean.parseBoolean((String) ev);
        res.setEmail(email);
        res.setVerified(emailVerified != null && emailVerified);
        return res;
    }

    /**
     * Call Google's tokeninfo endpoint to validate ID token.
     * Returns the token info map or null if invalid.
     */
    private Map<String, Object> verifyIdTokenWithGoogle(String idToken) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + java.net.URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            int status = conn.getResponseCode();
            BufferedReader in;
            if (status >= 200 && status < 300) {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                // invalid token
                return null;
            }
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            String body = sb.toString();
            return objectMapper.readValue(body, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public UserDto.Response getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<UserDto.Response> getAll() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserDto.Response update(Long id, UserDto.UpdateRequest req) {
        User user = findById(id);
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getStatus() != null) user.setStatus(req.getStatus());
        return toResponse(userRepository.save(user));
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    private UserDto.Response toResponse(User u) {
        UserDto.Response r = new UserDto.Response();
        r.setId(u.getId());
        r.setEmail(u.getEmail());
        r.setFullName(u.getFullName());
        r.setStatus(u.getStatus());
        r.setImg(u.getImg());
        return r;
    }
}
