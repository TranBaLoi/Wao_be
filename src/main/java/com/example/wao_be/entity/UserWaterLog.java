package com.example.wao_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_water_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserWaterLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Lượng nước uống mỗi lần (ml) */
    @Column(name = "amount_ml", nullable = false)
    private Integer amountMl;

    /** Thời điểm uống nước (giờ cụ thể) */
    @Column(name = "log_time", nullable = false)
    private LocalDateTime logTime;

    /** Ngày uống nước (để query theo ngày nhanh hơn) */
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @PrePersist
    public void setLogDate() {
        if (logTime != null && logDate == null) {
            logDate = logTime.toLocalDate();
        }
    }
}

