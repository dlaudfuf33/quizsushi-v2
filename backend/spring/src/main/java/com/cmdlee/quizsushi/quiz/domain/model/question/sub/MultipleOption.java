package com.cmdlee.quizsushi.quiz.domain.model.question.sub;

import com.cmdlee.quizsushi.quiz.domain.model.question.QuestionMultiple;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateMultipleOptionData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "multiple_option")
public class MultipleOption {

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
    private QuestionMultiple question;

    @Column(nullable = false)
    private String text;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    public MultipleOption(String text, boolean isCorrect) {
        this.text = text;
        this.isCorrect = isCorrect;
    }

    public MultipleOption(QuestionMultiple questionMultiple, String text, boolean isCorrect) {
        this.question = questionMultiple;
        this.text = text;
        this.isCorrect = isCorrect;
    }


    public static MultipleOption of(QuestionMultiple questionMultiple, UpdateMultipleOptionData option) {
        return new MultipleOption(
                questionMultiple,
                option.getText(),
                option.isCorrect()
        );
    }

    public void setQuestion(QuestionMultiple question) {
        this.question = question;
    }
}