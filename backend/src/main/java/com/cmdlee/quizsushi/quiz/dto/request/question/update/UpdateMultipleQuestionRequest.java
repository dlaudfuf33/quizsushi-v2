package com.cmdlee.quizsushi.quiz.dto.request.question.update;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class UpdateMultipleQuestionRequest extends UpdateQuestionRequest {

    @NotEmpty
    private List<UpdateMultipleOptionData> optionDataList;
}
