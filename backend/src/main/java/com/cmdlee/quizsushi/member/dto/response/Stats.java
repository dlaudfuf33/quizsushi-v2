package com.cmdlee.quizsushi.member.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Stats {
    private int totalQuizzesSolved;
    private int totalQuizzesCreated;
    private double averageScore;
}
