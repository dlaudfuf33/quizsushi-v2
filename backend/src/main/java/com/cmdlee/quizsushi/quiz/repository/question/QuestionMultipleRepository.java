package com.cmdlee.quizsushi.quiz.repository.question;

import com.cmdlee.quizsushi.quiz.domain.model.question.QuestionMultiple;
import com.cmdlee.quizsushi.quiz.repository.question.custom.QuestionMultipleRepositoryCustom;

import java.util.List;

public interface QuestionMultipleRepository extends BaseQuestionRepository<QuestionMultiple>, QuestionMultipleRepositoryCustom {
    List<QuestionMultiple> findByQuizId(Long quizId);
}
