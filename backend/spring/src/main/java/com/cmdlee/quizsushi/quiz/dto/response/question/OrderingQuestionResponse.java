package com.cmdlee.quizsushi.quiz.dto.response.question;

import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderingQuestionResponse implements QuestionResponse {
    private long id;
    private int no;
    private QuestionType type;
    private String subject;
    private String questionText;
    private List<String> orderingOptionList;

    @Builder
    public OrderingQuestionResponse(long id, int no, QuestionType type, String subject, String questionText, List<String> orderingOptionList) {
        this.id = id;
        this.no = no;
        this.type = type;
        this.subject = subject;
        this.questionText = questionText;
        this.orderingOptionList = orderingOptionList;
    }

}
