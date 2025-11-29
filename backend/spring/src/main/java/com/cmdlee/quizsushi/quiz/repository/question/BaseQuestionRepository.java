package com.cmdlee.quizsushi.quiz.repository.question;

import com.cmdlee.quizsushi.quiz.domain.model.question.BaseQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface BaseQuestionRepository<T extends BaseQuestion> extends JpaRepository<T, Long> {
    List<T> findByQuizId(Long quizId);

    Class<T> getDomainClass();
}
