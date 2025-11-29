package com.cmdlee.quizsushi.quiz.domain.factory;

import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.dto.QuizCreationData;

public interface QuizFactory {
    Quiz create(QuizCreationData data);
}
