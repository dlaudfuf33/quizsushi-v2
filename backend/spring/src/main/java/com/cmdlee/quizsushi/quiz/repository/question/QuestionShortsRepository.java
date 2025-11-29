package com.cmdlee.quizsushi.quiz.repository.question;

import com.cmdlee.quizsushi.quiz.domain.model.question.QuestionShorts;

import java.util.List;

public interface QuestionShortsRepository extends BaseQuestionRepository<QuestionShorts> {
    List<QuestionShorts> findByQuizId(Long quizId);
}
