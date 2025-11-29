package com.cmdlee.quizsushi.quiz.repository;

import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.domain.model.QuizRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRatingRepository extends JpaRepository<QuizRating, Long> {

    Optional<QuizRating> findByQuizAndMember(Quiz quiz, QuizsushiMember member);
}
