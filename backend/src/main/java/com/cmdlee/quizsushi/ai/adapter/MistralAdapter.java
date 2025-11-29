package com.cmdlee.quizsushi.ai.adapter;

import com.cmdlee.quizsushi.ai.prompt.PromptBuilder;
import com.cmdlee.quizsushi.ai.router.AiInstanceRouter;
import com.cmdlee.quizsushi.global.config.ai.AiProperties;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.dto.request.GenerateQuizRequest;
import com.cmdlee.quizsushi.quiz.dto.response.GenerateQuizResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

import static com.cmdlee.quizsushi.global.exception.ErrorCode.AI_COMMUNICATION_FAILED;
import static com.cmdlee.quizsushi.global.exception.ErrorCode.AI_EMPTY_RESPONSE;

@Slf4j
@Component
@RequiredArgsConstructor
public class MistralAdapter implements AiModelAdapter {
    public static final String MODEL_NAME = "mistarl";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final AiProperties aiProperties;

    private final AiInstanceRouter instanceRouter;
    private final PromptBuilder<GenerateQuizRequest> promptBuilder;

    @Override
    public String getModelName() {
        return MODEL_NAME;
    }

    @Override
    public List<GenerateQuizResponse> generateQuiz(GenerateQuizRequest request) {
        String prompt = promptBuilder.build("quiz_generation", request);
        String targetUrl = instanceRouter.nextUrl();
        String aiRaw;

        try {
            aiRaw = webClient.post()
                    .uri(targetUrl + "/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "model", MODEL_NAME,
                            "prompt", prompt,
                            "stream", aiProperties.isStream()
                    ))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (aiRaw == null || aiRaw.isBlank()) {
                throw new GlobalException(AI_EMPTY_RESPONSE);
            }

        } catch (Exception e) {
            log.error("[MISTRAL ERROR] Failed to call AI", e);
            throw new GlobalException(AI_COMMUNICATION_FAILED, e);
        }
        log.trace("[🧠 AI Raw Response]: {}", aiRaw);
        List<GenerateQuizResponse> generateQuizResponseList = parse(aiRaw);

        generateQuizResponseList.forEach(response ->
                log.trace("[🧪 GENERATED QUIZ] {}", response)
        );
        return generateQuizResponseList;
    }


    private List<GenerateQuizResponse> parse(String aiRaw) {
        try {
            Map<String, Object> aiRawMap = objectMapper.readValue(aiRaw, new TypeReference<>() {
            });
            String responseStr = objectMapper.convertValue(aiRawMap.get("response"), String.class);

            int startIdx = responseStr.indexOf('[');
            int endIdx = responseStr.lastIndexOf(']');
            if (startIdx == -1 || endIdx == -1 || startIdx >= endIdx) {
                throw new GlobalException(ErrorCode.AI_RESPONSE_PARSE_FAILED);
            }

            String jsonArray = responseStr.substring(startIdx, endIdx + 1);

            return List.of(objectMapper.readValue(jsonArray, GenerateQuizResponse[].class));
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.AI_RESPONSE_PARSE_FAILED, e);
        }
    }
}
