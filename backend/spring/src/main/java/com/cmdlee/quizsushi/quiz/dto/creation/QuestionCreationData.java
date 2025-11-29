package com.cmdlee.quizsushi.quiz.dto.creation;

import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;

public sealed interface QuestionCreationData permits
        MultipleCreationData,
        ShortsCreationData,
        OrderingCreationData,
        MatchingCreationData {

    Integer getNo();

    QuestionType getType();

    String getSubject();

    String getQuestionText();

    String getExplanation();
}

