package com.cmdlee.quizsushi.quiz.dto.request.question.update;

import lombok.Getter;

@Getter
public class UpdateMultipleOptionData {
    private Long id;

    private String text;
    private boolean isCorrect;
}
