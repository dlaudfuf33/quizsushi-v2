package com.cmdlee.quizsushi.report.dto.response;

import com.cmdlee.quizsushi.report.model.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportDetailResponse {
    private Long id;
    private String reason;
    private String email;
    private String nickname;
    private String title;
    private String message;
    private boolean isRead;
    private String targetType;
    private Long targetId;
    private LocalDateTime createdAt;

    private ReportTargetRsponse target;

    public static ReportDetailResponse of(Report report, ReportTargetRsponse targetResponse) {

        return ReportDetailResponse.builder()
                .id(report.getId())
                .reason(report.getReason().name())
                .title(report.getTitle())
                .message(report.getMessage())
                .nickname(report.getReporter() != null ? report.getReporter().getNickname() : "-")
                .email(report.getReporter() != null ? report.getReporter().getEmail() : "-")
                .isRead(report.isRead())
                .createdAt(report.getCreatedAt())
                .targetType(report.getTargetType() != null ? report.getTargetType().name() : null)
                .targetId(report.getTargetId())
                .target(targetResponse)
                .build();
    }
}
