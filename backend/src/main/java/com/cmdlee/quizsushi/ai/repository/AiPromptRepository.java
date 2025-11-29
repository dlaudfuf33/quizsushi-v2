package com.cmdlee.quizsushi.ai.repository;

import com.cmdlee.quizsushi.ai.domain.model.AiPrompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiPromptRepository extends JpaRepository<AiPrompt, Long> {
    Optional<AiPrompt> findFirstByNameOrderByUpdatedAtDesc(String quizGeneration);
}
