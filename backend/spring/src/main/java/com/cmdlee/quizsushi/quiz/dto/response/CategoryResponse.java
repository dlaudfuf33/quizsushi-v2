package com.cmdlee.quizsushi.quiz.dto.response;

import com.cmdlee.quizsushi.quiz.domain.model.Category;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CategoryResponse {
    private long id;
    private String title;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .title(category.getTitle())
                .build();
    }
}
