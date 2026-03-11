package com.example.wao_be.dto;

import com.example.wao_be.entity.WorkoutProgram;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

public class WorkoutProgramDto {

    @Data
    public static class Request {
        @NotBlank
        private String name;

        @NotNull
        private WorkoutProgram.ProgramLevel level;

        private Integer estimatedDuration;
        private String description;
        private List<ProgramExerciseDto.Request> exercises;
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        private WorkoutProgram.ProgramLevel level;
        private Integer estimatedDuration;
        private String description;
        private List<ProgramExerciseDto.Response> exercises;
    }
}

