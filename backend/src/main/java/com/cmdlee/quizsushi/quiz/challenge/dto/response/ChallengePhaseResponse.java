package com.cmdlee.quizsushi.quiz.challenge.dto.response;

import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeSession;
import com.cmdlee.quizsushi.quiz.challenge.model.enums.ChallengePhaseType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChallengePhaseResponse {
    private int currentRound;
    private ChallengePhaseType phase;

    public static ChallengePhaseResponse from(ChallengeSession challengeSession) {
        return ChallengePhaseResponse.builder()
                .currentRound(challengeSession.getCurrentRound())
                .phase(challengeSession.getPhase())
                .build();
    }
}
