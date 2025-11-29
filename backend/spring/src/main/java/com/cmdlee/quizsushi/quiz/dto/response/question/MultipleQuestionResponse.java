package com.cmdlee.quizsushi.quiz.dto.response.question;

import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class MultipleQuestionResponse implements QuestionResponse {
    private long id;
    private Integer no;
    private QuestionType type;
    private String subject;
    private String questionText;
    private List<MultipleOptionResponse> optionList;

    @Builder
    public MultipleQuestionResponse(long id, int no, QuestionType type, String subject, String questionText,
                                    List<MultipleOptionResponse> optionList) {
        this.id = id;
        this.no = no;
        this.type = type;
        this.subject = subject;
        this.questionText = questionText;
        this.optionList = optionList;
    }
}