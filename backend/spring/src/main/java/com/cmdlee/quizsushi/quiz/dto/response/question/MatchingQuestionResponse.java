package com.cmdlee.quizsushi.quiz.dto.response.question;

import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class MatchingQuestionResponse implements QuestionResponse {
    private long id;
    private Integer no;
    private QuestionType type;
    private String subject;
    private String questionText;
    private List<String> leftTextList;
    private List<String> rightTextList;

    @Builder
    public MatchingQuestionResponse(long id, int no, QuestionType type, String subject, String questionText, List<String> leftList, List<String> rightList) {
        this.id = id;
        this.no = no;
        this.type = type;
        this.subject = subject;
        this.questionText = questionText;
        this.leftTextList = leftList;
        this.rightTextList = rightList;
    }
}