package com.cmdlee.quizsushi.quiz.domain.model;

import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class QuizRating extends TimeBaseEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "quiz_rating_seq"
    )
    @SequenceGenerator(
            name = "quiz_rating_seq",
            sequenceName = "quiz_rating_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private QuizsushiMember member;

    @Column(nullable = false)
    private int rating;

    @Builder
    public QuizRating(Quiz quiz, QuizsushiMember member, int rating) {
        this.quiz = quiz;
        this.member = member;
        this.rating = rating;
    }

    public void updateRating(int newRating) {
        this.rating = newRating;
    }
}