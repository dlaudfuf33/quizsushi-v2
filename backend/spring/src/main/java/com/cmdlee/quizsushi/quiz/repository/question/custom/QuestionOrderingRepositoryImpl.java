package com.cmdlee.quizsushi.quiz.repository.question.custom;

import com.cmdlee.quizsushi.quiz.domain.model.question.QuestionOrdering;

public class QuestionOrderingRepositoryImpl implements QuestionOrderingRepositoryCustom {
    @Override
    public Class<QuestionOrdering> getDomainClass() {
        return QuestionOrdering.class;
    }
}
