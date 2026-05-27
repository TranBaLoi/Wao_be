/*
 * Bài làm của Nguyễn Hải Nam-B22DCCN558
 * Entity lưu từng lần cập nhật cân nặng của người dùng.
 */
package com.example.wao_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Ánh xạ bảng weight_logs, dùng để theo dõi lịch sử thay đổi cân nặng theo thời gian.
 */
@Entity
@Table(name = "weight_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeightLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user nào
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // cân nặng trước
    @Column(name = "old_weight")
    private Double oldWeight;

    // cân nặng sau khi update
    @Column(name = "new_weight", nullable = false)
    private Double newWeight;

    // chênh lệch
    @Column(name = "change_amount")
    private Double changeAmount;

    // ghi chú (optional)
    @Column(name = "note")
    private String note;

    // thời điểm log
    //namthem
    @Column(name = "logged_at", updatable = false)
    private LocalDateTime loggedAt;

    /**
     * Tự điền thời điểm log nếu thiếu và tính chênh lệch giữa cân nặng mới với cân nặng cũ.
     */
    @PrePersist
    public void calculateChange() {
        //namthem
        if (loggedAt == null) {
            loggedAt = LocalDateTime.now();
        }
        if (oldWeight != null && newWeight != null) {
            changeAmount = newWeight - oldWeight;
        }
    }
}
