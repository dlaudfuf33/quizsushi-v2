package com.cmdlee.quizsushi.quiz.repository.question;

import com.cmdlee.quizsushi.quiz.domain.model.question.QuestionOrdering;

import java.util.List;

public interface QuestionOrderingRepository extends BaseQuestionRepository<QuestionOrdering> {
    List<QuestionOrdering> findByQuizId(Long quizId);
}
