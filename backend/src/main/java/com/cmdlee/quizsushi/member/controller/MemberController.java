package com.cmdlee.quizsushi.member.controller;

import com.cmdlee.quizsushi.global.dto.CommonApiResponse;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.global.util.RejectBot;
import com.cmdlee.quizsushi.member.dto.request.UpdateProfileRequest;
import com.cmdlee.quizsushi.member.dto.response.CreatedQuizPageResponse;
import com.cmdlee.quizsushi.member.dto.response.MeResponse;
import com.cmdlee.quizsushi.member.dto.response.MemberProfileResponse;
import com.cmdlee.quizsushi.member.dto.response.SolvedQuizPageResponse;
import com.cmdlee.quizsushi.global.config.security.member.CustomMemberDetails;
import com.cmdlee.quizsushi.member.service.AuthService;
import com.cmdlee.quizsushi.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RejectBot
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;
    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<CommonApiResponse<MeResponse>> getMe(
            @AuthenticationPrincipal CustomMemberDetails memberDetails) {
        long memberId = getAuthenticatedMemberId(memberDetails);
        MeResponse response = memberService.getMemberMe(memberId);
        return ResponseEntity.ok(CommonApiResponse.ok(response, "유저 조회 성공"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<CommonApiResponse<Void>> deleteMe(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            HttpServletRequest request, HttpServletResponse response) {
        long memberId = getAuthenticatedMemberId(memberDetails);
        memberService.deleteMe(memberId);
        authService.logout(request, response);
        return ResponseEntity.ok(CommonApiResponse.ok(null, "탈퇴 성공"));
    }

    @GetMapping("/profiles")
    public ResponseEntity<CommonApiResponse<MemberProfileResponse>> getProfile(
            @AuthenticationPrincipal CustomMemberDetails memberDetails) {
        long memberId = getAuthenticatedMemberId(memberDetails);
        MemberProfileResponse memberProfileResponse = memberService.getProfile(memberId);
        return ResponseEntity.ok(CommonApiResponse.ok(memberProfileResponse, "프로필 조회 성공"));
    }

    @PutMapping("/profiles")
    public ResponseEntity<CommonApiResponse<MemberProfileResponse>> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal CustomMemberDetails memberDetails) {
        long memberId = getAuthenticatedMemberId(memberDetails);
        memberService.updateProfile(memberId, request);
        return ResponseEntity.ok(CommonApiResponse.ok(null, "프로필 수정 성공"));
    }


    @GetMapping("/quizzes/solved")
    public ResponseEntity<CommonApiResponse<SolvedQuizPageResponse>> getSolvedQuizzes(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        long memberId = getAuthenticatedMemberId(memberDetails);
        SolvedQuizPageResponse response = memberService.getSolvedQuizzes(memberId, page, size);
        return ResponseEntity.ok(CommonApiResponse.ok(response, "풀었던 문제 조회 성공"));
    }

    @GetMapping("/quizzes/created")
    public ResponseEntity<CommonApiResponse<CreatedQuizPageResponse>> getCreatedQuizzes(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        long memberId = getAuthenticatedMemberId(memberDetails);
        CreatedQuizPageResponse response = memberService.getCreatedQuizzes(memberId, page, size);
        return ResponseEntity.ok(CommonApiResponse.ok(response, "만든 문제 조회 성공"));
    }


    private Long getAuthenticatedMemberId(CustomMemberDetails details) {
        if (details == null) throw new GlobalException(ErrorCode.UNAUTHORIZED);
        return details.getId();
    }

}
