package com.cmdlee.quizsushi.quiz.domain.model.question;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateQuestionRequest;
import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateShortsQuestionRequest;
import com.cmdlee.quizsushi.quiz.dto.response.question.QuestionResponse;
import com.cmdlee.quizsushi.quiz.dto.response.question.ShortsQuestionResponse;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@NoArgsConstructor
@Table(name = "question_shorts")
public class QuestionShorts extends BaseQuestion {
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "shorts_answer",
            joinColumns = @JoinColumn(name = "question_id")
    )
    @Column(name = "answer", nullable = false)
    private List<String> correctAnswerList = new ArrayList<>();

    public QuestionShorts(Integer no, String subject, String questionText, String explanation, Quiz quiz,
                          List<String> correctAnswerList) {
        super(no, QuestionType.SHORTS, subject, questionText, explanation, quiz);
        this.correctAnswerList = correctAnswerList;
    }

    @Override
    public boolean equalsContent(UpdateQuestionRequest req) {
        if (!(req instanceof UpdateShortsQuestionRequest r)) return false;

        return this.getNo() == r.getNo()
                && Objects.equals(this.getSubject(), r.getSubject())
                && Objects.equals(this.getQuestionText(), r.getQuestionText())
                && Objects.equals(this.getExplanation(), r.getExplanation())
                && correctAnswerListEquals(this.correctAnswerList, r.getCorrectAnswerList());
    }

    private boolean correctAnswerListEquals(List<String> oldCorrectAnswerList, List<String> newCorrectAnswerList) {
        if (oldCorrectAnswerList.size() != newCorrectAnswerList.size()) return false;

        for (int i = 0; i < oldCorrectAnswerList.size(); i++) {
            String oldAnswer = oldCorrectAnswerList.get(i);
            String newAnswer = newCorrectAnswerList.get(i);
            if (!oldAnswer.equals(newAnswer)) return false;
        }
        return true;
    }

    @Override
    public void updateFrom(UpdateQuestionRequest req) {
        if (!(req instanceof UpdateShortsQuestionRequest r)) {
            throw new GlobalException(ErrorCode.INVALID_QUESTION_TYPE);
        }

        this.no = r.getNo();
        this.subject = r.getSubject();
        this.questionText = r.getQuestionText();
        this.explanation = r.getExplanation();

        this.correctAnswerList.clear();
        this.correctAnswerList.addAll(r.getCorrectAnswerList());
    }

    @Override
    public QuestionResponse toResponse() {
        return ShortsQuestionResponse.builder()
                .id(this.getId())
                .no(this.getNo())
                .type(this.getType())
                .subject(this.getSubject())
                .questionText(this.getQuestionText())
                .build();
    }

}
