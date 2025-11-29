package com.cmdlee.quizsushi.quiz.challenge.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GradingResult {
    private boolean isCorrect;
    private String comment;
}
