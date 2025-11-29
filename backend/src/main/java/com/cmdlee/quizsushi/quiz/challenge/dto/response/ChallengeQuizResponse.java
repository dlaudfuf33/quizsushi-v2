package com.cmdlee.quizsushi.quiz.challenge.dto.response;

import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeSession;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ChallengeQuizResponse {
    private String question;
    private String explain;

    private Instant givenAt;
    private int limitTime;
    private Instant deadlineTime;

    public static ChallengeQuizResponse from(ChallengeSession session) {
        if(session.getCurrentQuiz() !=null){
            return ChallengeQuizResponse.builder()
                    .question(session.getCurrentQuiz().getQuestion())
                    .explain(session.getCurrentQuiz().getExplain())
                    .limitTime(session.getCurrentQuiz().getLimitTime())
                    .givenAt(session.getCurrentQuiz().getGivenAt())
                    .deadlineTime(session.getCurrentQuiz().getDeadlineTime())
                    .build();
        }else{
            return ChallengeQuizResponse.builder().build();
        }

    }
}
