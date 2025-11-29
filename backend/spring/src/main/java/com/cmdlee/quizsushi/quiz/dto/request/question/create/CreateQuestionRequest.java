package com.cmdlee.quizsushi.quiz.dto.request.question.create;

import com.cmdlee.quizsushi.quiz.domain.model.enums.QuestionType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateMultipleQuestionRequest.class, name = "MULTIPLE"),
        @JsonSubTypes.Type(value = CreateShortsQuestionRequest.class, name = "SHORTS"),
        @JsonSubTypes.Type(value = CreateOrderingQuestionRequest.class, name = "ORDERING"),
        @JsonSubTypes.Type(value = CreateMatchingQuestionRequest.class, name = "MATCHING")
})
@Getter
public abstract class CreateQuestionRequest {
    @NotNull
    private QuestionType type;

    private int no;

    private String subject;

    @NotNull
    private String questionText;

    private String explanation;
}
