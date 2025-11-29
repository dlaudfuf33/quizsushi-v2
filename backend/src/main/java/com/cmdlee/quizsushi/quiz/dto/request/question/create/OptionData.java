package com.cmdlee.quizsushi.quiz.dto.request.question.create;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OptionData {
    @NotBlank
    private String text;

    private boolean isCorrect;
}
