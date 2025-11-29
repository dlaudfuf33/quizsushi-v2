package com.cmdlee.quizsushi.quiz.challenge.dto.response;

import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeSession;
import com.cmdlee.quizsushi.quiz.challenge.model.dto.BroadcastMessage;
import lombok.Builder;
import lombok.Data;

import java.util.Comparator;
import java.util.List;

@Data
@Builder
public class GameStateResponse {
    // 게임 진행상태
    private ChallengePhaseResponse phase;
    private ChallengeQuizResponse currentQuiz;
    // 게임 로그
    private List<BroadcastMessage> broadcastLog;
    // 채팅창
    private List<ChatMessageResponse> chatLog;
    // 전체 유저들의 상태 (점수 순위)
    private List<PlayerStateResponse> playerState;
    // AI 퀴즈 및 응답
    private ChallengeQuizResponse currentQuestion;

    public static GameStateResponse from(ChallengeSession session) {
        ChallengePhaseResponse phase = ChallengePhaseResponse.from(session);

        List<PlayerStateResponse> playerStateResponses = session.getPlayers().values().stream()
                .map(PlayerStateResponse::of)
                .sorted(Comparator.comparingLong(PlayerStateResponse::getScore).reversed())
                .toList();

        List<ChatMessageResponse> chatLogs = session.getChatLog().stream()
                .map(ChatMessageResponse::from)
                .sorted(Comparator.comparing(ChatMessageResponse::getChatAt))
                .toList();

        ChallengeQuizResponse currentQuestion = ChallengeQuizResponse.from(session);

        return GameStateResponse.builder()
                .phase(phase)
                .broadcastLog(session.getBroadcastLog())
                .chatLog(chatLogs)
                .playerState(playerStateResponses)
                .currentQuestion(currentQuestion)
                .build();
    }
}