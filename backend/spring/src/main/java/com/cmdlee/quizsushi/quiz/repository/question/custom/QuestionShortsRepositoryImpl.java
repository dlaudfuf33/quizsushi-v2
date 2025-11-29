package com.cmdlee.quizsushi.quiz.repository.question.custom;

import com.cmdlee.quizsushi.quiz.domain.model.question.QuestionShorts;

public class QuestionShortsRepositoryImpl implements QuestionShortsRepositoryCustom {
    @Override
    public Class<QuestionShorts> getDomainClass() {
        return QuestionShorts.class;
    }
}
