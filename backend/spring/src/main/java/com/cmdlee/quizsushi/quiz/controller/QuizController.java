package com.cmdlee.quizsushi.quiz.controller;

import com.cmdlee.quizsushi.global.config.security.member.CustomMemberDetails;
import com.cmdlee.quizsushi.global.dto.CommonApiResponse;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.global.util.RejectBot;
import com.cmdlee.quizsushi.quiz.dto.request.CreateQuizRequest;
import com.cmdlee.quizsushi.quiz.dto.request.QuizRatingRequest;
import com.cmdlee.quizsushi.quiz.dto.request.QuizResultRequest;
import com.cmdlee.quizsushi.quiz.dto.request.UpdateQuizRequest;
import com.cmdlee.quizsushi.quiz.dto.response.*;
import com.cmdlee.quizsushi.quiz.service.CategoryService;
import com.cmdlee.quizsushi.quiz.service.QuizService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quizzes")
@Slf4j
public class QuizController {
    private final CategoryService categoryService;
    private final QuizService quizService;

    @GetMapping("/categories/introductions")
    public ResponseEntity<CommonApiResponse<List<IntroductionCategoryResponse>>> getIntroductionCategories() {
        List<IntroductionCategoryResponse> introductions = categoryService.findIntroductionCategories();
        return ResponseEntity.ok(CommonApiResponse.ok(introductions, "카테고리 소개 목록 조회 성공"));
    }

    @GetMapping("/categories")
    public ResponseEntity<CommonApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.findAll();
        return ResponseEntity.ok(CommonApiResponse.ok(categories, "카테고리 목록 조회 성공"));
    }

    @GetMapping
    public ResponseEntity<CommonApiResponse<QuizPageResponse>> getQuizPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String searchQuery
    ) {
        QuizPageResponse responseData = quizService.getQuizPage(page, size, sort, searchType, searchQuery, categoryId);
        return ResponseEntity.ok(CommonApiResponse.ok(responseData, "퀴즈 목록 조회 성공"));
    }

    @RejectBot
    @PostMapping
    public ResponseEntity<CommonApiResponse<CreatedQuizResponse>> createQuiz(
            @Valid @RequestBody CreateQuizRequest request,
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        long memberId = getAuthenticatedMemberId(memberDetails);
        CreatedQuizResponse createQuizResponse = quizService.createQuiz(request, memberId);
        return ResponseEntity.ok(CommonApiResponse.ok(createQuizResponse, "퀴즈 생성 성공"));
    }


//    @RejectBot
    @GetMapping("/{id}")
    public ResponseEntity<CommonApiResponse<QuizDetailResponse>> getQuizById(
            @PathVariable Long id) {
        QuizDetailResponse quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(CommonApiResponse.ok(quiz, "퀴즈 조회 성공"));
    }

    @RejectBot
    @PatchMapping
    public ResponseEntity<CommonApiResponse<UpdatedQuizResponse>> updateQuizById(
            @Valid @RequestBody UpdateQuizRequest request,
            @AuthenticationPrincipal CustomMemberDetails memberDetails
    ) {
        long memberId = getAuthenticatedMemberId(memberDetails);
        UpdatedQuizResponse updateQuiz = quizService.updateQuiz(request, memberId);
        return ResponseEntity.ok(CommonApiResponse.ok(updateQuiz, "수정 성공"));
    }

    @RejectBot
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonApiResponse<Void>> deleteQuiz(
            @PathVariable("id") Long quizId,
            @AuthenticationPrincipal CustomMemberDetails memberDetails) {
        long memberId = getAuthenticatedMemberId(memberDetails);
        quizService.deleteQuiz(quizId, memberId);
        return ResponseEntity.ok(CommonApiResponse.ok(null, "삭제 성공"));
    }

    @RejectBot
    @PutMapping("/{id}/rate")
    public ResponseEntity<CommonApiResponse<Void>> rateQuiz(
            @PathVariable("id") Long quizId,
            @RequestBody @Valid QuizRatingRequest request,
            @AuthenticationPrincipal CustomMemberDetails memberDetails) {
        long memberId = getAuthenticatedMemberId(memberDetails);
        quizService.rateQuiz(quizId, memberId, request);
        return ResponseEntity.ok(CommonApiResponse.ok(null, "퀴즈 평점 등록 성공"));
    }

    @RejectBot
    @PostMapping("/{id}/results")
    public ResponseEntity<CommonApiResponse<Void>> submitQuizResult(
            @PathVariable("id") Long quizId,
            @RequestBody QuizResultRequest quizResultRequest,
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            HttpServletRequest request
    ) {
        if (memberDetails != null) {
            quizService.saveMemberQuizResult(quizId, memberDetails.getId(), quizResultRequest);
        } else {
            String ip = request.getRemoteAddr();
            String ua = request.getHeader("User-Agent");
            quizService.saveGuestQuizResult(quizId, quizResultRequest, ip, ua);
        }

        return ResponseEntity.ok(CommonApiResponse.ok(null, "퀴즈 풀이 기록 완료"));
    }

    private Long getAuthenticatedMemberId(CustomMemberDetails details) {
        if (details == null) throw new GlobalException(ErrorCode.UNAUTHORIZED);
        return details.getId();
    }
}
