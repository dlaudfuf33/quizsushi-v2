package com.cmdlee.quizsushi.quiz.domain.model.question.sub;

import com.cmdlee.quizsushi.quiz.domain.model.question.QuestionMatching;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateMatchingPairData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "matching_pair")
public class MatchingPair {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = ""
    )
    @SequenceGenerator(
            name = "",
            sequenceName = "",
            allocationSize = 1
    )
    private Long id;

    @JoinColumn(name = "question_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private QuestionMatching question;

    @Column(name = "left_text", nullable = false)
    private String leftText;

    @Column(name = "right_text", nullable = false)
    private String rightText;

    public MatchingPair(QuestionMatching question, String leftText, String rightText) {
        this.question = question;
        this.leftText = leftText;
        this.rightText = rightText;
    }

    public static MatchingPair of(QuestionMatching questionMatching, UpdateMatchingPairData newPair) {
        return new MatchingPair(
                questionMatching,
                newPair.getLeftText(),
                newPair.getRightText()
        );
    }
}
