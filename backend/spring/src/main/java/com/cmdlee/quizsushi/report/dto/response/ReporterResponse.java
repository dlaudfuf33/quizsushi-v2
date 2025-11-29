package com.cmdlee.quizsushi.report.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public  class ReporterResponse {
    private Long id;
    private String email;
}
