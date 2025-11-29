package com.cmdlee.quizsushi.admin.dto.response;

import com.cmdlee.quizsushi.report.dto.response.ReportResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReportPageResponse {

    private final List<ReportResponse> reports;
    private final int totalPages;
}
