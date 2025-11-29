package com.cmdlee.quizsushi.quiz.dto.request.question.create;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateMatchingQuestionRequest extends CreateQuestionRequest {

    @NotEmpty
    private List<MatchingPairData> matchingPairDataList;
}
