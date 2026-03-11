package com.example.wao_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "daily_summaries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(DailySummary.DailySummaryId.class)
public class DailySummary {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    /** Tổng calo nạp vào trong ngày (từ UserFoodLog) */
    @Column(name = "total_cal_in")
    @Builder.Default
    private Double totalCalIn = 0.0;

    /** Tổng calo tiêu hao trong ngày (từ UserWorkoutLog) */
    @Column(name = "total_cal_out")
    @Builder.Default
    private Double totalCalOut = 0.0;

    /** Tổng nước uống trong ngày (ml, từ UserWaterLog) */
    @Column(name = "total_water")
    @Builder.Default
    private Integer totalWater = 0;

    /** Tổng bước chân trong ngày (từ StepLog) */
    @Column(name = "total_steps")
    @Builder.Default
    private Integer totalSteps = 0;

    /** Có đạt mục tiêu ngày không */
    @Column(name = "is_goal_achieved")
    @Builder.Default
    private Boolean isGoalAchieved = false;

    // =====================
    // Composite Key Class
    // =====================
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySummaryId implements Serializable {
        private Long user;
        private LocalDate logDate;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DailySummaryId that)) return false;
            return Objects.equals(user, that.user) && Objects.equals(logDate, that.logDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, logDate);
        }
    }
}

