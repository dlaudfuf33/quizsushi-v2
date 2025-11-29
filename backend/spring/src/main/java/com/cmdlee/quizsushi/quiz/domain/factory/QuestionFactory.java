package com.cmdlee.quizsushi.quiz.domain.factory;

import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.domain.model.question.BaseQuestion;
import com.cmdlee.quizsushi.quiz.dto.creation.QuestionCreationData;

public interface QuestionFactory {
    BaseQuestion create(QuestionCreationData data, Quiz quiz);
}
