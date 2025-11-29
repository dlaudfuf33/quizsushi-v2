package com.cmdlee.quizsushi.quiz.dto.request;

import com.cmdlee.quizsushi.quiz.dto.request.question.update.UpdateQuestionRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateQuizRequest {
    @NotNull
    private Long id;

    private String description;

    private boolean useSubject;

    @NotEmpty(message = "문제 리스트는 비어 있을 수 없습니다.")
    private List<UpdateQuestionRequest> questions;
}
