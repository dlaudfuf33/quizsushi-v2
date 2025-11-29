package com.cmdlee.quizsushi.ai.service;

import com.cmdlee.quizsushi.ai.prompt.DbPromptTemplateProvider;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeQuiz;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiService {
    private static final List<String> QUESTION_TYPES = List.of(
            "코드 실행 결과 (Java)",
            "코드 실행 결과 (Python)",
            "코드 실행 결과 (SQL)",
            "코드 실행 결과 (C)",
            "네트워크 개념 설명",
            "보안 개념 설명",
            "알고리즘 흐름 기반 문제"
    );
    private static final int MAX_RETRIES = 3;
    private final ObjectMapper objectMapper;

    private final VertexAiGeminiChatModel vertexAiGeminiChatModel;
    private final DbPromptTemplateProvider dbPromptTemplateProvider;


    public ChallengeQuiz generateChallengeQuizWithGemini(int level) {
        String basePrompt = dbPromptTemplateProvider.getTemplate("quizChallenge_generation_gemini");
        String randomType = QUESTION_TYPES.get(ThreadLocalRandom.current().nextInt(QUESTION_TYPES.size()));
        String finalPrompt = basePrompt.replace("{{randomType}}", randomType)
                .replace("{{level}}", String.valueOf(level));

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return callAndParse(finalPrompt);
            } catch (GlobalException e) {
                log.warn("[GeminiAiService] Attempt {} failed: {}", attempt, e.getMessage());
                if (attempt == MAX_RETRIES) throw e;
            } catch (Exception e) {
                log.error("[GeminiAiService] Unexpected error on attempt {}", attempt, e);
                if (attempt == MAX_RETRIES) throw new GlobalException(ErrorCode.AI_RESPONSE_PARSE_FAILED, e);
            }
        }

        throw new GlobalException(ErrorCode.AI_EMPTY_RESPONSE);
    }

    private ChallengeQuiz callAndParse(String prompt) {
        String result = vertexAiGeminiChatModel.call(prompt);
        System.out.println("삭제요망 RAW :" + result);

        if (result == null || result.isBlank()) {
            throw new GlobalException(ErrorCode.AI_EMPTY_RESPONSE);
        }

        return result.trim().startsWith("```json")
                ? parseChallengeQuizFromMarkdown(result)
                : parseChallengeQuizFromJson(result);
    }


    private ChallengeQuiz parseChallengeQuizFromJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);

            String question = cleanMarkdown(root.get("question").asText());
            JsonNode answerNode = root.get("correctAnswer");
            List<String> answerList = objectMapper.convertValue(answerNode, new TypeReference<>() {
            });
            String explanation = cleanMarkdown(root.get("explanation").asText().replace("\\n", "\n"));
            int score = root.has("score") ? root.get("score").asInt() : 0;
            int limitTime = root.has("limitTime") ? root.get("limitTime").asInt() : 60;


            return ChallengeQuiz.of(question, answerList, limitTime, explanation,  score);

        } catch (Exception e) {
            throw new GlobalException(ErrorCode.AI_RESPONSE_PARSE_FAILED, e);
        }
    }

    private ChallengeQuiz parseChallengeQuizFromMarkdown(String markdown) {
        try {
            Pattern pattern = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(markdown);

            if (!matcher.find()) {
                throw new GlobalException(ErrorCode.AI_JSON_BLOCK_NOT_FOUND);
            }

            String jsonPart = matcher.group(1).trim();
            return parseChallengeQuizFromJson(jsonPart);

        } catch (Exception e) {
            throw new GlobalException(ErrorCode.AI_RESPONSE_PARSE_FAILED, e);
        }
    }

    private String cleanMarkdown(String input) {
        return input.replace("\\\\n", "\n").replace("\\\\", "");
    }

}
