package com.cmdlee.quizsushi.quiz.dto.request.question.update;


import lombok.Getter;

@Getter
public class UpdateOrderingOptionData {
    private Long id;

    private String text;
    private int ordering;
}