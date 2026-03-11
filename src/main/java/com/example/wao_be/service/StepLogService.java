package com.example.wao_be.service;

import com.example.wao_be.dto.StepLogDto;
import com.example.wao_be.entity.StepLog;
import com.example.wao_be.entity.User;
import com.example.wao_be.repository.StepLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StepLogService {

    private final StepLogRepository stepLogRepository;
    private final UserService userService;

    public StepLogDto.Response log(Long userId, StepLogDto.Request req) {
        User user = userService.findById(userId);
        // Nếu đã có log trong ngày thì cập nhật, không tạo mới
        StepLog stepLog = stepLogRepository.findByUserIdAndLogDate(userId, req.getLogDate())
                .orElse(StepLog.builder().user(user).logDate(req.getLogDate()).build());
        stepLog.setStepCount(req.getStepCount());
        return toResponse(stepLogRepository.save(stepLog));
    }

    @Transactional(readOnly = true)
    public List<StepLogDto.Response> getByUserAndDateRange(Long userId, LocalDate from, LocalDate to) {
        return stepLogRepository.findByUserIdAndLogDateBetween(userId, from, to)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StepLogDto.Response getByUserAndDate(Long userId, LocalDate date) {
        return stepLogRepository.findByUserIdAndLogDate(userId, date)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("No step log for user " + userId + " on " + date));
    }

    private StepLogDto.Response toResponse(StepLog s) {
        StepLogDto.Response r = new StepLogDto.Response();
        r.setId(s.getId());
        r.setUserId(s.getUser().getId());
        r.setStepCount(s.getStepCount());
        r.setLogDate(s.getLogDate());
        return r;
    }
}

