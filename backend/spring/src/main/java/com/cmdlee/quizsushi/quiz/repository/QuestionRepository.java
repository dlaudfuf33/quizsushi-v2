package com.cmdlee.quizsushi.quiz.repository;

import com.cmdlee.quizsushi.quiz.domain.model.Question;
import com.cmdlee.quizsushi.quiz.domain.model.question.BaseQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Modifying
    @Query("DELETE FROM Question q WHERE q.quiz.id = :quizId")
    void deleteByQuizId(@Param("quizId") Long quizId);

    List<BaseQuestion> findByQuizId(Long id);
}
