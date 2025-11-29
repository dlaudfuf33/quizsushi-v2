package com.cmdlee.quizsushi.report.service;

import com.cmdlee.quizsushi.admin.dto.response.ReportPageResponse;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.member.repository.MemberRepository;
import com.cmdlee.quizsushi.report.dto.request.ReportRequest;
import com.cmdlee.quizsushi.report.dto.response.ReportResponse;
import com.cmdlee.quizsushi.report.handler.ReportTargetHandler;
import com.cmdlee.quizsushi.report.model.Report;
import com.cmdlee.quizsushi.report.model.enums.ReportStatusType;
import com.cmdlee.quizsushi.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {


    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;
    private final List<ReportTargetHandler> handlers;

    @Transactional
    public void createReport(Long memberId, ReportRequest request) {
        QuizsushiMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND));

        ReportTargetHandler handler = handlers.stream()
                .filter(h -> h.getTargetType() == request.getTargetType())
                .findFirst()
                .orElseThrow(() -> new GlobalException(ErrorCode.UNSUPPORTED_REPORT_TARGET));
        handler.validateTargetExists(request.getTargetId());

        Report report = Report.builder()
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .title(request.getTitle())
                .message(request.getMessage())
                .reason(request.getReason())
                .reporter(member)
                .status(ReportStatusType.PENDING)
                .build();

        reportRepository.save(report);
    }


    public ReportPageResponse getReports(String searchQuery, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        String likeQuery = (searchQuery == null || searchQuery.isBlank()) ? null : "%" + searchQuery + "%";
        ReportStatusType statusEnum = parseStatus(status);
        Page<Report> reportPage = reportRepository.searchReports(likeQuery, statusEnum, pageable);

        List<ReportResponse> reports = reportPage.getContent().stream()
                .map(ReportResponse::from)
                .toList();

        return new ReportPageResponse(reports, reportPage.getTotalPages());
    }

    private ReportStatusType parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return ReportStatusType.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new GlobalException(ErrorCode.UNSUPPORTED_REPORT_STATUS);
        }
    }

    @Transactional
    public void markAsRead(Long id) {
        Report report = reportRepository.findById(id).orElseThrow(
                () -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND)
        );
        report.reading();
    }

    @Transactional
    public void updateStatus(Long id, String newStatus) {
        Report report = reportRepository.findById(id).orElseThrow(
                () -> new GlobalException(ErrorCode.ENTITY_NOT_FOUND)
        );
        report.updateStatus(ReportStatusType.valueOf(newStatus));
    }
}
