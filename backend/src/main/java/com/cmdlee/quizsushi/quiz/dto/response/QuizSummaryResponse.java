package com.cmdlee.quizsushi.quiz.dto.response;

import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuizSummaryResponse {
    private String id;
    private String title;
    private String authorName;
    private String category;
    private String categoryId;
    private String categoryIcon;
    private int questionCount;
    private double rating;
    private Long ratingCount;
    private Long viewCount;
    private Long solveCount;
    private double averageScore;


    public static QuizSummaryResponse from(Quiz quiz) {
        return QuizSummaryResponse.builder()
                .id(quiz.getId().toString())
                .title(quiz.getTitle())
                .authorName(quiz.getAuthor().getNickname())
                .category(quiz.getCategory().getTitle())
                .categoryId(quiz.getCategory().getId().toString())
                .categoryIcon(quiz.getCategory().getIcon())
                .questionCount(quiz.getQuestionCount())
                .rating(quiz.getRating())
                .ratingCount(quiz.getRatingCount())
                .viewCount(quiz.getViewCount())
                .solveCount(quiz.getSolveCount())
                .build();
    }
}
