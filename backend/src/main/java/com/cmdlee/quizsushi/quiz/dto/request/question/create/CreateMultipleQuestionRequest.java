package com.cmdlee.quizsushi.quiz.dto.request.question.create;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateMultipleQuestionRequest extends CreateQuestionRequest {

    @NotNull
    private boolean shuffleOption;

    @NotEmpty
    private List<OptionData> optionDataList;
}