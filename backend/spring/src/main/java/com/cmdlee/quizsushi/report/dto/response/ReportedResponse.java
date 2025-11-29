package com.cmdlee.quizsushi.report.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportedResponse {
    private String type;
    private Long id;
    private String targetName;
    private String reason;
}