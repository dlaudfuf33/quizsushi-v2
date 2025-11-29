package com.cmdlee.quizsushi.quiz.dto.request.question.update;


import lombok.Getter;

@Getter
public class UpdateMatchingPairData {
    private Long id;

    private String leftText;
    private String rightText;
}
