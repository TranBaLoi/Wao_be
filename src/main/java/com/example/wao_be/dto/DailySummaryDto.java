package com.example.wao_be.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailySummaryDto {
    private Long userId;
    private LocalDate logDate;
    private Double totalCalIn;
    private Double totalCalOut;
    private Double netCalories;   // calIn - calOut
    private Integer totalWater;
    private Integer totalSteps;
    private Boolean isGoalAchieved;
}

