package com.cmdlee.quizsushi.quiz.challenge.repository;

import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeToggle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeToggleRepository extends JpaRepository<ChallengeToggle, Long> {
    Optional<ChallengeToggle> findTopByOrderByUpdatedAtDesc();
}


