package com.example.wao_be.service;

import com.example.wao_be.dto.UserDto;
import com.example.wao_be.entity.User;
import com.example.wao_be.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

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
        return r;
    }
}

