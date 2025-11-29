package com.cmdlee.quizsushi.report.handler;

import com.cmdlee.quizsushi.report.model.enums.ReportTargetType;

public interface ReportTargetHandler {
    ReportTargetType getTargetType();
    void validateTargetExists(Long targetId);
}
