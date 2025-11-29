package com.cmdlee.quizsushi.quiz.domain.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class GuestQuizSolveLog {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "guest_quiz_solve_log_seq"
    )
    @SequenceGenerator(
            name = "guest_quiz_solve_log_seq",
            sequenceName = "guest_quiz_solve_log_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    private String guestIp;

    private String guestUa;

    private Integer score;

    private final LocalDateTime submittedAt = LocalDateTime.now();


    @Builder
    public GuestQuizSolveLog(Quiz quiz, String guestIp, String guestUa, Integer score) {
        this.quiz = quiz;
        this.guestIp = guestIp;
        this.guestUa = guestUa;
        this.score = score;
    }


}