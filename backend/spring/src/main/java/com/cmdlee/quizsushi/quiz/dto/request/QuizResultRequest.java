package com.cmdlee.quizsushi.quiz.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultRequest {
    @Min(0)
    @Max(100)
    private Integer score;
}