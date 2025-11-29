package com.cmdlee.quizsushi.quiz.domain.model.question;

import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateQuestionRequest;
import com.cmdlee.quizsushi.quiz.dto.response.question.QuestionResponse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor
public abstract class BaseQuestion {
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
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    protected Quiz quiz;

    @Column(name = "question_no", nullable = false)
    protected Integer no;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    protected QuestionType type;

    @Column(name = "subject")
    protected String subject;

    @Column(name = "question_text", nullable = false)
    protected String questionText;

    @Column(name = "explanation", columnDefinition = "TEXT")
    protected String explanation;

    protected BaseQuestion(Integer no, QuestionType type, String subject, String questionText, String explanation, Quiz quiz) {
        this.no = no;
        this.type = type;
        this.subject = subject;
        this.questionText = questionText;
        this.explanation = explanation;
        this.quiz = quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public abstract boolean equalsContent(UpdateQuestionRequest req);

    public abstract void updateFrom(UpdateQuestionRequest req);

    public abstract QuestionResponse toResponse();

}
