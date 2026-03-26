package com.example.wao_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Danh gia muc do kho de dat muc tieu dua tren so calories can hap thu moi ngay.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyCalorieBreakdownDto {

    private Double dailyCalories;
    private DifficultyLevel difficultyLevel;
    private String note;

    public enum DifficultyLevel {
        EASY,
        MEDIUM,
        HARD
    }

    public static DailyCalorieBreakdownDto fromDailyCalories(Double dailyCalories) {
        if (dailyCalories == null || dailyCalories < 0) {
            return DailyCalorieBreakdownDto.builder()
                    .dailyCalories(dailyCalories)
                    .difficultyLevel(DifficultyLevel.MEDIUM)
                    .note("Khong du du lieu de danh gia do kho")
                    .build();
        }

        double absDailyCalories = Math.abs(dailyCalories);
        DifficultyLevel level;
        String note;

        if (absDailyCalories <= 300) {
            level = DifficultyLevel.EASY;
            note = "Muc tieu de, toc do thay doi can nang nhe";
        } else if (absDailyCalories <= 700) {
            level = DifficultyLevel.MEDIUM;
            note = "Muc tieu trung binh, can theo doi va ky luat";
        } else {
            level = DifficultyLevel.HARD;
            note = "Muc tieu kho, toc do thay doi can nang cao";
        }

        return DailyCalorieBreakdownDto.builder()
                .dailyCalories(dailyCalories)
                .difficultyLevel(level)
                .note(note)
                .build();
    }
}
