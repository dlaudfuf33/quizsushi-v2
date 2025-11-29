package com.cmdlee.quizsushi.quiz.dto.request.question.update;

import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UpdateMultipleQuestionRequest.class, name = "MULTIPLE"),
        @JsonSubTypes.Type(value = UpdateShortsQuestionRequest.class, name = "SHORTS"),
        @JsonSubTypes.Type(value = UpdateOrderingQuestionRequest.class, name = "ORDERING"),
        @JsonSubTypes.Type(value = UpdateMatchingQuestionRequest.class, name = "MATCHING")
})
@Getter
public abstract class UpdateQuestionRequest {
    private Long id;

    private int no;
    private QuestionType type;
    private String subject;
    private String questionText;
    private String explanation;
    private boolean isDelete;
}
