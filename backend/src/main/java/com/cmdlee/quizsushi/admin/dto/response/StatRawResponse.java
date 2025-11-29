package com.cmdlee.quizsushi.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StatRawResponse {
    private final String label;
    private final LocalDateTime time;
    private final long count;
}
