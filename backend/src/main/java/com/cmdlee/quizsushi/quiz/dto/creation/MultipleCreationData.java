package com.cmdlee.quizsushi.quiz.dto.creation;

import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class MultipleCreationData implements QuestionCreationData {
    private final Integer no;
    private final QuestionType type;
    private final String subject;
    private final String questionText;
    private final String explanation;
    private final List<MultipleOptionData> multipleOptions;

    public static MultipleCreationData of(Integer no, String subject, String questionText,
                                          List<MultipleOptionData> options, String explanation) {
        return new MultipleCreationData(no, QuestionType.MULTIPLE, subject, questionText, explanation, options);
    }
}
