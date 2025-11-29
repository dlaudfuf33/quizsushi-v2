package com.cmdlee.quizsushi.ai.prompt;

import com.cmdlee.quizsushi.ai.repository.AiPromptRepository;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DbPromptTemplateProvider implements PromptTemplateProvider {

    private final AiPromptRepository promptRepository;

    @Override
    public String getTemplate(String taskName) {
        return promptRepository.findFirstByNameOrderByUpdatedAtDesc(taskName)
                .orElseThrow(() -> new GlobalException(ErrorCode.PROMPT_NOT_FOUND))
                .getTemplate();
    }
}
