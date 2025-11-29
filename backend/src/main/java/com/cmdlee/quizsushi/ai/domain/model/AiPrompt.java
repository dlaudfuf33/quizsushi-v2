package com.cmdlee.quizsushi.ai.domain.model;

import com.cmdlee.quizsushi.quiz.domain.model.TimeBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_prompts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiPrompt extends TimeBaseEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "ai_prompt_seq"
    )
    @SequenceGenerator(
            name = "ai_prompt_seq",
            sequenceName = "ai_prompt_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String template;

    private String description;

}
