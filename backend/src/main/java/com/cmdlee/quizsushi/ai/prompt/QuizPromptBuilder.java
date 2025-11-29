package com.cmdlee.quizsushi.ai.prompt;

import com.cmdlee.quizsushi.quiz.dto.request.GenerateQuizRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class QuizPromptBuilder implements PromptBuilder<GenerateQuizRequest> {

    private final PromptTemplateProvider templateProvider;

    @Override
    public String build(String taskName, GenerateQuizRequest request) {

        String template = templateProvider.getTemplate(taskName);
        Map<String, String> map = Map.of(
                "TOPIC", request.getTopic(),
                "DESCRIPTION", request.getDescription(),
                "COUNT", String.valueOf(request.getCount()),
                "DIFFICULTY", request.getDifficulty(),
                "QUESTIONTYPE", request.getQuestionType()
        );
        return new StringSubstitutor(map, "{{", "}}").replace(template);
    }
}