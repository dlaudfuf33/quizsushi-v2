package com.cmdlee.quizsushi.report.dto.request;


import com.cmdlee.quizsushi.report.model.enums.ReasonType;
import com.cmdlee.quizsushi.report.model.enums.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReportRequest {

    @NotNull(message = "신고 사유는 필수입니다.")
    private ReasonType reason;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @NotNull
    private ReportTargetType targetType;

    @NotNull
    private Long targetId;
}