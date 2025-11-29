package com.cmdlee.quizsushi.quiz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntroductionCategoryResponse {
    private long id;
    private String title;
    private String description;
    private Long count;
    private String icon;
}
