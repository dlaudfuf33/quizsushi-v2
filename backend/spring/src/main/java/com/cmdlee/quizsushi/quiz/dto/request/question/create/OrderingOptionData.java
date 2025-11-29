package com.cmdlee.quizsushi.quiz.dto.request.question.create;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderingOptionData {
    @NotBlank
    private String text;
    private int ordering;
}