package com.cmdlee.quizsushi.quiz.repository.question.custom;

import com.cmdlee.quizsushi.quiz.domain.model.question.QuestionMatching;

public class QuestionMatchingRepositoryImpl implements QuestionMatchingRepositoryCustom {
    @Override
    public Class<QuestionMatching> getDomainClass() {
        return QuestionMatching.class;
    }
}
