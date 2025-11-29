package com.cmdlee.quizsushi.ai.adapter;

import com.cmdlee.quizsushi.quiz.dto.request.GenerateQuizRequest;
import com.cmdlee.quizsushi.quiz.dto.response.GenerateQuizResponse;

import java.util.List;

public interface AiModelAdapter {
    String getModelName();
    List<GenerateQuizResponse> generateQuiz(GenerateQuizRequest request);
}
