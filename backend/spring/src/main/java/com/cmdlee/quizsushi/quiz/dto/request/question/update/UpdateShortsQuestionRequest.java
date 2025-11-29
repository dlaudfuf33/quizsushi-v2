package com.cmdlee.quizsushi.quiz.dto.request.question.update;

import lombok.Getter;

import java.util.List;

@Getter
public class UpdateShortsQuestionRequest extends UpdateQuestionRequest {
    private List<String> correctAnswerList;
}
