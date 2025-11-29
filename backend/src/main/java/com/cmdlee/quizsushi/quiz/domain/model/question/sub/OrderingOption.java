package com.cmdlee.quizsushi.quiz.domain.model.question.sub;

import com.cmdlee.quizsushi.quiz.domain.model.question.QuestionOrdering;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateOrderingOptionData;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "ordering_option")
public class OrderingOption {

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
    private QuestionOrdering question;

    @Column(nullable = false)
    private String text;

    @Column(name = "ordering", nullable = false)
    private int ordering;

    public OrderingOption(QuestionOrdering question, String text, int ordering) {
        this.question = question;
        this.text = text;
        this.ordering = ordering;
    }

    public static OrderingOption of(QuestionOrdering questionOrdering, UpdateOrderingOptionData newOpt) {
        return new OrderingOption(
                questionOrdering,
                newOpt.getText(),
                newOpt.getOrdering()
        );
    }
}