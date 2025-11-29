package com.cmdlee.quizsushi.quiz.dto.creation;

import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import com.cmdlee.quizsushi.quiz.dto.request.question.create.OrderingOptionData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class OrderingCreationData implements QuestionCreationData {
    private final Integer no;
    private final QuestionType type;
    private final String subject;
    private final String questionText;
    private final String explanation;
    private final List<OrderingOptionData> orderingOptionDataList;

    public static OrderingCreationData of(Integer no, String subject, String questionText,
                                          List<OrderingOptionData> options, String explanation) {
        return new OrderingCreationData(no, QuestionType.ORDERING, subject, questionText, explanation, options);
    }
}
