package com.cmdlee.quizsushi.quiz.repository.question.custom;

import com.cmdlee.quizsushi.quiz.domain.model.question.QuestionMultiple;

public class QuestionMultipleRepositoryImpl implements QuestionMultipleRepositoryCustom{
    @Override
    public Class<QuestionMultiple> getDomainClass() {
        return QuestionMultiple.class;
    }
}
