package com.cmdlee.quizsushi.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MemberPageResponse {
    private List<MemberResponse> members;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}
