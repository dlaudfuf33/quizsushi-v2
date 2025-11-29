package com.cmdlee.quizsushi.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuizPageResponse {
    private List<QuizSummaryResponse> quizzes;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}
