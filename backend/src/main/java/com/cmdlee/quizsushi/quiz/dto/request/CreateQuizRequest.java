package com.cmdlee.quizsushi.quiz.dto.request;

import com.cmdlee.quizsushi.quiz.dto.request.question.create.CreateQuestionRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateQuizRequest {

    @NotNull
    private String title;
    
    @NotNull
    private Long categoryId;

    private String description;

    private boolean useSubject;

    @NotEmpty(message = "문제 리스트는 비어 있을 수 없습니다.")
    private List<CreateQuestionRequest> questions;
}
