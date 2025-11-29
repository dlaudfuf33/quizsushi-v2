package com.cmdlee.quizsushi.global.config.websocket;

import com.cmdlee.quizsushi.global.config.security.member.CustomMemberDetails;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeSession;
import com.cmdlee.quizsushi.quiz.challenge.service.ChallengeSessionRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {
    private final ChallengeSessionRedisService challengeSessionRedisService;

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();
        if (command == null) return message;

        if (StompCommand.SUBSCRIBE.equals(command)) {
            String destination = accessor.getDestination();
            if (destination == null) {
                throw new GlobalException(ErrorCode.INVALID_REQUEST);
            }
            if (destination.startsWith("/topic/leaderboard") || destination.startsWith("/topic/messages") ||
                    destination.startsWith("/topic/matching")) {
                return message;
            }
            String sessionId = extractSessionId(destination);
            if (sessionId.isBlank()) {
                log.info("Skipping session check for general subscription: {}", destination);
                return message;
            }

            Authentication authentication = (Authentication) accessor.getUser();
            if (authentication == null || !(authentication.getPrincipal() instanceof CustomMemberDetails memberDetails)) {
                throw new GlobalException(ErrorCode.UNAUTHORIZED_ACCESS);
            }
            accessor.setUser(authentication);

            String memberId = memberDetails.getId().toString();
            ChallengeSession session = challengeSessionRedisService.getSession(sessionId);
            if (session == null) {
                log.warn("Subscription denied - session not found: sessionId={}", sessionId);
                throw new GlobalException(ErrorCode.SESSION_NOT_FOUND);
            }

            log.info("Subscription granted - memberId={}, sessionId={}", memberId, sessionId);
        }

        return message;
    }

    private String extractSessionId(String destination) {
        return destination.substring(destination.lastIndexOf("/") + 1);
    }
}