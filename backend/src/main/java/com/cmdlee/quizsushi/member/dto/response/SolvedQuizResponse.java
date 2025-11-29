package com.cmdlee.quizsushi.member.dto.response;

import com.cmdlee.quizsushi.quiz.domain.model.MemberQuizSolveLog;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SolvedQuizResponse {
    private Long id;
    private String title;
    private Integer score;
    private String date;
    private String category;

    public static SolvedQuizResponse from(MemberQuizSolveLog log) {
        boolean isDeleted = log.getQuiz() == null;

        return SolvedQuizResponse.builder()
                .id(isDeleted ? null : log.getQuiz().getId())
                .title(isDeleted ? "삭제된 퀴즈입니다" : log.getQuiz().getTitle())
                .score(log.getScore())
                .date(log.getSubmittedAt().toLocalDate().toString())
                .category(isDeleted ? "알 수 없음" : log.getQuiz().getCategory().getTitle())
                .build();
    }
}