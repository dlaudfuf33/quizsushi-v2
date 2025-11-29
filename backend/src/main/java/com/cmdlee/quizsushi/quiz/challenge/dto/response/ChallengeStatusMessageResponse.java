package com.cmdlee.quizsushi.quiz.challenge.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChallengeStatusMessageResponse {
    private String sessionId;
    private String error;
    private GameStateResponse sessionState;

    public static ChallengeStatusMessageResponse of(String sessionId, GameStateResponse state) {
        return ChallengeStatusMessageResponse.builder()
                .sessionId(sessionId)
                .sessionState(state)
                .build();
    }

}