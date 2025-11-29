package com.cmdlee.quizsushi.quiz.dto.response;

import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.domain.model.question.BaseQuestion;
import com.cmdlee.quizsushi.quiz.dto.response.question.QuestionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizDetailResponse {
    private Long id;
    private Author author;
    private String title;
    private String description;
    private boolean useSubject;
    private CategoryResponse category;
    private double rating;
    private long ratingCount;
    private List<QuestionResponse> questions;

    public static QuizDetailResponse of(Quiz quiz, List<BaseQuestion> questionList) {
        Author author = Author.builder()
                .id(quiz.getAuthor().getId().toString())
                .nickName(quiz.getAuthor().getNickname())
                .avatar(quiz.getAuthor().getProfileImage())
                .build();

        List<QuestionResponse> questions = questionList.stream()
                .map(BaseQuestion::toResponse)
                .toList();

        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(quiz.getCategory().getId())
                .title(quiz.getCategory().getTitle())
                .build();


        return QuizDetailResponse.builder()
                .id(quiz.getId())
                .author(author)
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .useSubject(quiz.isUseSubject())
                .category(categoryResponse)
                .rating(quiz.getRating())
                .ratingCount(quiz.getRatingCount())
                .questions(questions)
                .build();
    }


}