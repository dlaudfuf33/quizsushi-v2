package com.cmdlee.quizsushi.quiz.domain.model;

import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Question extends TimeBaseEntity {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "question_seq"
    )
    @SequenceGenerator(
            name = "question_seq",
            sequenceName = "question_seq",
            allocationSize = 50
    )
    private Long id;

    private int no;

    @Column(length = 100)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;


    @Column
    private String options;

    // 객관식 정답
    @Column(name = "correct_answers")
    private String correctIndexes;

    // 주관식 정답
    private String correctAnswerText;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Builder
    public Question(int no, String subject, QuestionType type, String questionText, List<String> options,
                    List<Integer> correctIndexes, String correctAnswerText, String explanation, Quiz quiz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.no = no;
            this.subject = subject;
            this.type = type;
            this.questionText = questionText;
            this.options = options != null ? mapper.writeValueAsString(options) : "[]";
            this.correctIndexes = correctIndexes != null ? mapper.writeValueAsString(correctIndexes) : "[]";
            this.correctAnswerText = correctAnswerText;
            this.explanation = explanation;
            this.quiz = quiz;
        } catch (Exception e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
    }

}