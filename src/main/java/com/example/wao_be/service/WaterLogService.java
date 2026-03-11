package com.example.wao_be.service;

import com.example.wao_be.dto.WaterLogDto;
import com.example.wao_be.entity.User;
import com.example.wao_be.entity.UserWaterLog;
import com.example.wao_be.repository.UserWaterLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WaterLogService {

    private final UserWaterLogRepository waterLogRepository;
    private final UserService userService;

    public WaterLogDto.Response log(Long userId, WaterLogDto.Request req) {
        User user = userService.findById(userId);
        UserWaterLog waterLog = UserWaterLog.builder()
                .user(user)
                .amountMl(req.getAmountMl())
                .logTime(req.getLogTime())
                .logDate(req.getLogTime().toLocalDate())
                .build();
        return toResponse(waterLogRepository.save(waterLog));
    }

    @Transactional(readOnly = true)
    public List<WaterLogDto.Response> getByUserAndDate(Long userId, LocalDate date) {
        return waterLogRepository.findByUserIdAndLogDate(userId, date)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Integer getTotalWaterByUserAndDate(Long userId, LocalDate date) {
        return waterLogRepository.sumWaterByUserIdAndLogDate(userId, date);
    }

    public void delete(Long logId) {
        if (!waterLogRepository.existsById(logId)) {
            throw new EntityNotFoundException("WaterLog not found: " + logId);
        }
        waterLogRepository.deleteById(logId);
    }

    private WaterLogDto.Response toResponse(UserWaterLog w) {
        WaterLogDto.Response r = new WaterLogDto.Response();
        r.setId(w.getId());
        r.setUserId(w.getUser().getId());
        r.setAmountMl(w.getAmountMl());
        r.setLogTime(w.getLogTime());
        r.setLogDate(w.getLogDate());
        return r;
    }
}

