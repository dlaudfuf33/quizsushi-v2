package com.cmdlee.quizsushi.quiz.dto.response.question;

import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ShortsQuestionResponse implements QuestionResponse {
    private long id;
    private Integer no;
    private QuestionType type;
    private String subject;
    private String questionText;

    @Builder
    public ShortsQuestionResponse(long id, int no, QuestionType type, String subject, String questionText) {
        this.id = id;
        this.no = no;
        this.type = type;
        this.subject = subject;
        this.questionText = questionText;
    }
}
