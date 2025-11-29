package com.cmdlee.quizsushi.quiz.repository.question;

import com.cmdlee.quizsushi.quiz.domain.model.question.QuestionMatching;

import java.util.List;

public interface QuestionMatchingRepository extends BaseQuestionRepository<QuestionMatching> {
    List<QuestionMatching> findByQuizId(Long quizId);
}
