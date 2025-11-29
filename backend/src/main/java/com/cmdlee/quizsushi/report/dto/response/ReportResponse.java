package com.cmdlee.quizsushi.report.dto.response;

import com.cmdlee.quizsushi.report.model.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ReportResponse {

    private Long id;
    private String title;
    private String message;
    private boolean read;
    private String status;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    private ReporterResponse reporter;
    private ReportedResponse reported;

    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .title(report.getTitle())
                .message(report.getMessage())
                .read(report.isRead())
                .status(report.getStatus().name())
                .createdAt(report.getCreatedAt().toLocalDate())
                .updatedAt(report.getUpdatedAt().toLocalDate())

                .reporter(ReporterResponse.builder()
                        .id(report.getReporter() != null ? report.getReporter().getId() : null)
                        .email(report.getReporter() != null ? report.getReporter().getEmail() : "익명")
                        .build())

                .reported(ReportedResponse.builder()
                        .type(report.getTargetType() != null ? report.getTargetType().name() : null)
                        .id(report.getTargetId())
                        .targetName(report.getTargetType().name())
                        .reason(report.getReason().name())
                        .build())

                .build();
    }
}