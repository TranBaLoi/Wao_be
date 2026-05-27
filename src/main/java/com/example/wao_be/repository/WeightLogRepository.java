/*
 * Bài làm của Nguyễn Hải Nam-B22DCCN558
 * Repository phục vụ truy vấn lịch sử cân nặng cho module thống kê.
 */
// Nam them respository 
package com.example.wao_be.repository;

import com.example.wao_be.entity.WeightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Khai báo các truy vấn Spring Data JPA cần dùng cho weight_logs.
 */
@Repository
public interface WeightLogRepository extends JpaRepository<WeightLog, Long> {
    /** Lấy toàn bộ log cân nặng của user trong khoảng thời gian, sắp xếp tăng dần để dựng chuỗi biểu đồ. */
    List<WeightLog> findByUserIdAndLoggedAtBetweenOrderByLoggedAtAsc(
            Long userId,
            LocalDateTime from,
            LocalDateTime to);

    //namthem
    /** Tìm log gần nhất trước hoặc đúng thời điểm cần ghi log để xác định oldWeight. */
    Optional<WeightLog> findFirstByUserIdAndLoggedAtLessThanEqualOrderByLoggedAtDesc(
            Long userId,
            LocalDateTime loggedAt);

    //namthem
    /** Lấy bản ghi cân nặng mới nhất của user, dùng cho màn hình hiển thị cân nặng hiện tại. */
    Optional<WeightLog> findFirstByUserIdOrderByLoggedAtDesc(Long userId);

    //namthem
    /** Kiểm tra user đã có log cân nặng trong ngày chưa để tránh tạo trùng bản ghi. */
    boolean existsByUserIdAndLoggedAtBetween(
            Long userId,
            LocalDateTime from,
            LocalDateTime to);
}
