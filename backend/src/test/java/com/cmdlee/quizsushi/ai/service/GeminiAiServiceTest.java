package com.cmdlee.quizsushi.ai.service;

import com.cmdlee.quizsushi.ai.prompt.DbPromptTemplateProvider;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeQuiz;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeminiAiServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private VertexAiGeminiChatModel vertexAiGeminiChatModel;

    @Mock
    private DbPromptTemplateProvider dbPromptTemplateProvider;

    @InjectMocks
    private GeminiAiService geminiAiService;

    private final String mockPrompt = "Generate a quiz for {{level}} level on {{randomType}}";
    private final String validJsonResponse = "{\"question\":\"Test Question\",\"correctAnswer\":[\"Test Answer\"],\"explanation\":\"Test Explanation\",\"score\":10,\"limitTime\":30,\"onCreated\":\"Hello\",\"onProgress\":\"Progress\"}";
    private final String validMarkdownJsonResponse = "```json\n" + validJsonResponse + "\n```";

    @BeforeEach
    void setUp() {
        when(dbPromptTemplateProvider.getTemplate(anyString())).thenReturn(mockPrompt);
    }

    @DisplayName("Gemini AI가 유효한 퀴즈를 생성하는지 테스트")
    @Test
    void generateChallengeQuizWithGemini_ValidResponse_ReturnsChallengeQuiz() throws Exception {
        // Given
        when(vertexAiGeminiChatModel.call(anyString())).thenReturn(validJsonResponse);
        ObjectNode mockRootNode = new ObjectMapper().createObjectNode();
        mockRootNode.put("question", "Test Question");
        mockRootNode.putArray("correctAnswer").add("Test Answer");
        mockRootNode.put("explanation", "Test Explanation");
        mockRootNode.put("score", 10);
        mockRootNode.put("limitTime", 30);
        mockRootNode.put("onCreated", "Hello");
        mockRootNode.put("onProgress", "Progress");
        when(objectMapper.readTree(anyString())).thenReturn(mockRootNode);
        when(objectMapper.convertValue(any(), any(TypeReference.class))).thenReturn(List.of("Test Answer"));


        // When
        ChallengeQuiz quiz = geminiAiService.generateChallengeQuizWithGemini(1);

        // Then
        assertThat(quiz).isNotNull();
        assertThat(quiz.getQuestion()).isEqualTo("Test Question");
        assertThat(quiz.getAnswerList()).containsExactly("Test Answer");
        assertThat(quiz.getExplain()).isEqualTo("Test Explanation");
        assertThat(quiz.getScore()).isEqualTo(10);
        assertThat(quiz.getLimitTime()).isEqualTo(30);
    }

    @DisplayName("Gemini AI가 마크다운 형식의 유효한 퀴즈를 생성하는지 테스트")
    @Test
    void generateChallengeQuizWithGemini_ValidMarkdownResponse_ReturnsChallengeQuiz() throws Exception {
        // Given
        when(vertexAiGeminiChatModel.call(anyString())).thenReturn(validMarkdownJsonResponse);
        ObjectNode mockRootNode = new ObjectMapper().createObjectNode();
        mockRootNode.put("question", "Test Question");
        mockRootNode.putArray("correctAnswer").add("Test Answer");
        mockRootNode.put("explanation", "Test Explanation");
        mockRootNode.put("score", 10);
        mockRootNode.put("limitTime", 30);
        mockRootNode.put("onCreated", "Hello");
        mockRootNode.put("onProgress", "Progress");
        when(objectMapper.readTree(anyString())).thenReturn(mockRootNode);
        when(objectMapper.convertValue(any(), any(TypeReference.class))).thenReturn(List.of("Test Answer"));

        // When
        ChallengeQuiz quiz = geminiAiService.generateChallengeQuizWithGemini(1);

        // Then
        assertThat(quiz).isNotNull();
        assertThat(quiz.getQuestion()).isEqualTo("Test Question");
        assertThat(quiz.getAnswerList()).containsExactly("Test Answer");
        assertThat(quiz.getExplain()).isEqualTo("Test Explanation");
        assertThat(quiz.getScore()).isEqualTo(10);
        assertThat(quiz.getLimitTime()).isEqualTo(30);
    }

    @DisplayName("Gemini AI 응답이 비어있을 때 GlobalException을 던지는지 테스트")
    @Test
    void generateChallengeQuizWithGemini_EmptyResponse_ThrowsGlobalException() {
        // Given
        when(vertexAiGeminiChatModel.call(anyString())).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> geminiAiService.generateChallengeQuizWithGemini(1))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AI_EMPTY_RESPONSE);
    }

    @DisplayName("Gemini AI 응답이 유효하지 않은 JSON 형식일 때 GlobalException을 던지는지 테스트")
    @Test
    void generateChallengeQuizWithGemini_InvalidJsonResponse_ThrowsGlobalException() throws Exception {
        // Given
        when(vertexAiGeminiChatModel.call(anyString())).thenReturn("invalid json");
        when(objectMapper.readTree(anyString())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Invalid JSON") {});

        // When & Then
        assertThatThrownBy(() -> geminiAiService.generateChallengeQuizWithGemini(1))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.AI_RESPONSE_PARSE_FAILED);
    }
}