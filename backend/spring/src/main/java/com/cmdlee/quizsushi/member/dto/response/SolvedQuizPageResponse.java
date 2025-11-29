package com.cmdlee.quizsushi.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SolvedQuizPageResponse {
    private List<SolvedQuizResponse> quizzes;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}