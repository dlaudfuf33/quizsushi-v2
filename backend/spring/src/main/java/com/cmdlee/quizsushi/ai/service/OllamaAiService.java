package com.cmdlee.quizsushi.ai.service;

import com.cmdlee.quizsushi.ai.adapter.AiModelAdapter;
import com.cmdlee.quizsushi.ai.router.AiModelRouter;
import com.cmdlee.quizsushi.quiz.dto.request.GenerateQuizRequest;
import com.cmdlee.quizsushi.quiz.dto.response.GenerateQuizResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaAiService {

    private final AiModelRouter aiModelRouter;

    public List<GenerateQuizResponse> generateQuizByAI(GenerateQuizRequest request) {
        AiModelAdapter adapter = aiModelRouter.getAdapter("llama3Adapter");
        return adapter.generateQuiz(request);
    }
}
