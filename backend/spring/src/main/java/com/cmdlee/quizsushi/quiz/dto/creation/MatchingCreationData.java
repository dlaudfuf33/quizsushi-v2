package com.cmdlee.quizsushi.quiz.dto.creation;

import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import com.cmdlee.quizsushi.quiz.dto.request.question.create.MatchingPairData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public final class MatchingCreationData implements QuestionCreationData {
    private final Integer no;
    private final QuestionType type;
    private final String subject;
    private final String questionText;
    private final String explanation;
    private final List<MatchingPairData> matchingPairs;

    public static MatchingCreationData of(Integer no, String subject, String questionText,
                                          List<MatchingPairData> pairs, String explanation) {
        return new MatchingCreationData(no, QuestionType.MATCHING, subject, questionText, explanation, pairs);
    }
}
