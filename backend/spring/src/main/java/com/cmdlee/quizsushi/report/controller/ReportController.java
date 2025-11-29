package com.cmdlee.quizsushi.report.controller;

import com.cmdlee.quizsushi.global.dto.CommonApiResponse;
import com.cmdlee.quizsushi.global.util.RejectBot;
import com.cmdlee.quizsushi.global.config.security.member.CustomMemberDetails;
import com.cmdlee.quizsushi.report.dto.request.ReportRequest;
import com.cmdlee.quizsushi.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RejectBot
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<CommonApiResponse<Void>> reportContent(
            @AuthenticationPrincipal CustomMemberDetails memberDetails,
            @Valid @RequestBody ReportRequest request
    ) {
        reportService.createReport(memberDetails.getId(), request);
        return ResponseEntity.ok(CommonApiResponse.ok(null, "신고가 접수되었습니다."));
    }
}
