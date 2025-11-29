package com.cmdlee.quizsushi.quiz.challenge.model;


import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeQuiz implements Serializable {
    private String question;
    private List<String> answerList;
    private String explain;
    private int score;

    private Instant givenAt;
    private int limitTime;
    private Instant deadlineTime;

    public static ChallengeQuiz of(String question, List<String> answerList, int limitTime, String explain, int score) {
        Instant now = Instant.now();
        return ChallengeQuiz.builder()
                .question(question)
                .answerList(answerList)
                .givenAt(now)
                .limitTime(limitTime)
                .deadlineTime(now.plusSeconds(limitTime))
                .explain(explain)
                .score(score)
                .build();
    }
}
