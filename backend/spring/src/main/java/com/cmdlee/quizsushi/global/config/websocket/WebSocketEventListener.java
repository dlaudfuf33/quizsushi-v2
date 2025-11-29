package com.cmdlee.quizsushi.global.config.websocket;

import com.cmdlee.quizsushi.global.config.security.member.CustomMemberDetails;
import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeSession;
import com.cmdlee.quizsushi.quiz.challenge.model.enums.ChallengePhaseType;
import com.cmdlee.quizsushi.quiz.challenge.service.ChallengeConnectionRedisService;
import com.cmdlee.quizsushi.quiz.challenge.service.ChallengeGameService;
import com.cmdlee.quizsushi.quiz.challenge.service.ChallengeSessionRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final ChallengeConnectionRedisService challengeConnectionRedisService;
    private final ChallengeSessionRedisService challengeSessionRedisService;
    private final ChallengeGameService challengeGameService;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String memberId = extractMemberId(accessor);
        String sessionId = accessor.getFirstNativeHeader("x-session-id");

        if (memberId != null && sessionId != null) {
            challengeConnectionRedisService.register(sessionId, memberId);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String memberId = extractMemberId(accessor);

        if (memberId == null) return;

        // 연결 정보 제거
        String sessionId = challengeConnectionRedisService.getSessionIdOf(memberId);
        challengeConnectionRedisService.unregister(memberId);

        // 세션에 존재하는 유저라면 강제 탈락 처리
        if (sessionId != null) {
            ChallengeSession session = challengeSessionRedisService.getSession(sessionId);
            if (session != null && session.getPlayers().containsKey(memberId)) {
                if (!session.isPlayerDead(memberId)) {
                    session.eliminatePlayer(memberId);
                    challengeSessionRedisService.saveSession(session);
                    challengeGameService.sendGameState(session);
                }

                // 방에 남은 유저가 없으면 세션 종료 처리
                long onlineCount = challengeConnectionRedisService.getOnlineCount(sessionId);
                if (onlineCount == 0) {
                    session.setPhase(ChallengePhaseType.GAMEOVER);
                    challengeSessionRedisService.saveSession(session);
                    challengeGameService.sendGameState(session);
                }
            }
        }
    }

    private String extractMemberId(StompHeaderAccessor accessor) {
        Authentication auth = (Authentication) accessor.getUser();
        if (auth == null || !(auth.getPrincipal() instanceof CustomMemberDetails member)) return null;
        return member.getId().toString();
    }
}
