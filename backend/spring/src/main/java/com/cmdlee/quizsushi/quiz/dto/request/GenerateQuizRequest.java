package com.cmdlee.quizsushi.quiz.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GenerateQuizRequest {
    private String topic;
    private String description;
    @Min(value = 1, message = "최소 1개의 문제를 생성해야 합니다.")
    @Max(value = 3, message = "최대 3개의 문제만 생성할 수 있습니다.")
    private int count;
    private String difficulty;
    private String questionType;
}