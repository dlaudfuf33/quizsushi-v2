package com.cmdlee.quizsushi.admin.controller;

import com.cmdlee.quizsushi.admin.dto.request.AdminCreateRequest;
import com.cmdlee.quizsushi.admin.dto.request.AdminInfoUpdateRequest;
import com.cmdlee.quizsushi.admin.dto.request.AdminRoleUpdateRequest;
import com.cmdlee.quizsushi.admin.dto.response.AdminResponse;
import com.cmdlee.quizsushi.admin.dto.response.MemberPageResponse;
import com.cmdlee.quizsushi.admin.dto.response.ReportPageResponse;
import com.cmdlee.quizsushi.admin.dto.response.StatRawResponse;
import com.cmdlee.quizsushi.global.config.security.admin.CustomAdminDetails;
import com.cmdlee.quizsushi.admin.service.AdminService;
import com.cmdlee.quizsushi.global.dto.CommonApiResponse;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.global.util.RejectBot;
import com.cmdlee.quizsushi.member.dto.response.MeResponse;
import com.cmdlee.quizsushi.quiz.challenge.service.ChallengeToggleService;
import com.cmdlee.quizsushi.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RejectBot
@RestController
@RequestMapping("/cmdlee-qs")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;
    private final ChallengeToggleService challengeToggleService;

    @GetMapping("/me")
    public ResponseEntity<CommonApiResponse<MeResponse>> getMe(
            @AuthenticationPrincipal CustomAdminDetails adminDetails) {
        if (adminDetails == null) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        MeResponse response = adminService.getAdminMe(adminDetails.getId());
        return ResponseEntity.ok(CommonApiResponse.ok(response, "관리자 조회 성공"));
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @GetMapping("/dashboard/data")
    public ResponseEntity<CommonApiResponse<List<StatRawResponse>>> getDashboardStats(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam("trunc") String truncType) {
        if (start.isAfter(end)) {
            throw new GlobalException(ErrorCode.WRONG_DATE_RANGE);
        }
        List<StatRawResponse> statRawResponses = adminService.getStats(start, end, truncType);
        return ResponseEntity.ok(CommonApiResponse.ok(statRawResponses, "통계 조회 성공"));
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @GetMapping("/admin")
    public ResponseEntity<CommonApiResponse<List<AdminResponse>>> getAllAdmins() {
        List<AdminResponse> adminResponseList = adminService.getAllAdmins();
        return ResponseEntity.ok(CommonApiResponse.ok(adminResponseList, "관리자 목록 조회 성공"));
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @PatchMapping("/admin/me")
    public ResponseEntity<CommonApiResponse<Void>> updateMyProfile(
            @AuthenticationPrincipal CustomAdminDetails adminDetails,
            @RequestBody AdminInfoUpdateRequest request) {
        adminService.updateMyProfile(adminDetails.getId(), request);
        return ResponseEntity.ok(CommonApiResponse.ok(null, "정보수정 성공"));
    }

    @PreAuthorize("hasRole('ROLE_ROOT')")
    @PostMapping("/admin")
    public ResponseEntity<CommonApiResponse<Void>> createAdmin(@RequestBody @Valid AdminCreateRequest request) {
        adminService.createAdmin(request);
        return ResponseEntity.ok(CommonApiResponse.ok(null, "관리자 생성 성공"));
    }

    @PreAuthorize("hasRole('ROLE_ROOT')")
    @PatchMapping("/admin/{id}/role")
    public ResponseEntity<CommonApiResponse<Void>> updateAdminRole(@PathVariable Long id,
                                                                   @RequestBody @Valid AdminRoleUpdateRequest request) {
        adminService.updateAdminRole(id, request.getRole());
        return ResponseEntity.ok(CommonApiResponse.ok(null, "관리자 권한수정 성공"));
    }

    @PreAuthorize("hasRole('ROLE_ROOT')")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<CommonApiResponse<Void>> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(CommonApiResponse.ok(null, "관리자 삭제 성공"));
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @GetMapping("/members")
    public ResponseEntity<CommonApiResponse<MemberPageResponse>> getMembers(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        MemberPageResponse members = adminService.findMembers(searchQuery, status, page, size);
        return ResponseEntity.ok(CommonApiResponse.ok(members, "조회 성공"));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping("/members/{id}/status")
    public ResponseEntity<CommonApiResponse<Void>> updateMemberStatus(@PathVariable Long id) {
        adminService.updateMemberStatus(id);
        return ResponseEntity.ok(CommonApiResponse.ok(null, "회원 계정상태 변경 성공"));
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @GetMapping("/reports")
    public ResponseEntity<CommonApiResponse<ReportPageResponse>> getReports(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        ReportPageResponse reportPage = reportService.getReports(searchQuery, status, page, size);
        return ResponseEntity.ok(CommonApiResponse.ok(reportPage, "신고 목록 조회 성공"));
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @PatchMapping("/reports/{id}/read")
    public ResponseEntity<Void> markReportAsRead(@PathVariable Long id) {
        reportService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @PatchMapping("/reports/{id}/status")
    public ResponseEntity<Void> updateReportStatus(@PathVariable Long id,
                                                   @RequestBody @Valid Map<String, String> body) {
        String newStatus = body.get("newStatus");
        reportService.updateStatus(id, newStatus);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/quizzes/{id}")
    public ResponseEntity<CommonApiResponse<Void>> deleteQuiz(@PathVariable Long id) {
        adminService.deleteQuiz(id);
        return ResponseEntity.ok(CommonApiResponse.ok(null, "퀴즈 삭제 성공"));
    }

    @PreAuthorize("hasRole('ROLE_VIEWER')")
    @GetMapping("/challenge/toggle")
    public ResponseEntity<CommonApiResponse<Boolean>> getToggleStatus() {
        boolean status = challengeToggleService.getCurrentStatus();
        return ResponseEntity.ok(CommonApiResponse.ok(status, "현재 챌린지 상태 조회 성공"));
    }

    @PreAuthorize("hasRole('ROLE_ROOT')")
    @PostMapping("/challenge/toggle")
    public ResponseEntity<CommonApiResponse<Void>> toggleChallenge(@AuthenticationPrincipal CustomAdminDetails adminDetails) {
        Long adminId = adminDetails.getId();
        challengeToggleService.toggleChallenge(adminId);
        return ResponseEntity.ok(CommonApiResponse.ok(null, "챌린지 상태가 변경되었습니다."));
    }


}