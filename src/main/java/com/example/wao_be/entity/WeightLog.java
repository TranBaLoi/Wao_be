package com.example.wao_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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
    @CreationTimestamp
    @Column(name = "logged_at", updatable = false)
    private LocalDateTime loggedAt;

    @PrePersist
    public void calculateChange() {
        if (oldWeight != null && newWeight != null) {
            changeAmount = newWeight - oldWeight;
        }
    }
}