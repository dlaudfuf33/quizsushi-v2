package com.cmdlee.quizsushi.quiz.domain.model;

import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class MemberQuizSolveLog {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "member_quiz_solve_log_seq"
    )
    @SequenceGenerator(
            name = "member_quiz_solve_log_seq",
            sequenceName = "member_quiz_solve_log_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private QuizsushiMember member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    private Integer score;

    private final LocalDateTime submittedAt = LocalDateTime.now();

    @Builder
    public MemberQuizSolveLog(Quiz quiz, QuizsushiMember member, Integer score) {
        this.quiz = quiz;
        this.member = member;
        this.score = score;
    }


}