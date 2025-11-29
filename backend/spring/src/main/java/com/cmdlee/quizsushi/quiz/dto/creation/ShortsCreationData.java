package com.cmdlee.quizsushi.quiz.dto.creation;

import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class ShortsCreationData implements QuestionCreationData {
    private final Integer no;
    private final QuestionType type;
    private final String subject;
    private final String questionText;
    private final String explanation;
    private final List<String> shortsAnswerList;

    public static ShortsCreationData of(Integer no, String subject, String questionText,
                                        List<String> correctAnswers, String explanation) {
        return new ShortsCreationData(no, QuestionType.SHORTS, subject, questionText, explanation, correctAnswers);
    }
}
