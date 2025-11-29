package com.cmdlee.quizsushi.quiz.dto.creation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class MultipleOptionData {
    private final String text;
    private final boolean isCorrect;
}
