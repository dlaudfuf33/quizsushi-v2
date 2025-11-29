package com.cmdlee.quizsushi.ai.controller;

import com.cmdlee.quizsushi.ai.service.OllamaAiService;
import com.cmdlee.quizsushi.global.config.security.member.CustomMemberDetails;
import com.cmdlee.quizsushi.global.dto.CommonApiResponse;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.global.util.RejectBot;
import com.cmdlee.quizsushi.quiz.dto.request.GenerateQuizRequest;
import com.cmdlee.quizsushi.quiz.dto.response.GenerateQuizResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RejectBot
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final OllamaAiService ollamaAiService;


    // ollama
    @PostMapping("/quizzes/questions")
    public ResponseEntity<CommonApiResponse<List<GenerateQuizResponse>>> generateQuizzes(
            @Valid @RequestBody GenerateQuizRequest request,
            @AuthenticationPrincipal CustomMemberDetails memberDetails) {
        getAuthenticatedMemberId(memberDetails);
        List<GenerateQuizResponse> generateQuizByAI = ollamaAiService.generateQuizByAI(request);
        return ResponseEntity.ok(CommonApiResponse.ok(generateQuizByAI, "생성 성공"));
    }

    private void getAuthenticatedMemberId(CustomMemberDetails details) {
        if (details == null) throw new GlobalException(ErrorCode.UNAUTHORIZED);
    }
}
