package com.cmdlee.quizsushi.quiz.challenge.controller;

import com.cmdlee.quizsushi.global.config.security.member.CustomMemberDetails;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.challenge.dto.request.ChallengeMessageRequest;
import com.cmdlee.quizsushi.quiz.challenge.dto.request.ChatMessageRequest;
import com.cmdlee.quizsushi.quiz.challenge.dto.response.MatchingMessageResponse;
import com.cmdlee.quizsushi.quiz.challenge.model.enums.MatchStatusType;
import com.cmdlee.quizsushi.quiz.challenge.service.ChallengeGameService;
import com.cmdlee.quizsushi.quiz.challenge.service.ChallengeToggleService;
import com.cmdlee.quizsushi.quiz.challenge.service.MatchingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChallengeWebSocketController {
    private static final String MATCHING_CHANNEL_PREFIX = "/topic/matching/";
    private static final String CHALLENGE_CHANNEL_PREFIX = "/topic/challenge/";

    private final ChallengeGameService challengeGameService;
    private final MatchingQueueService matchingQueueService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChallengeToggleService challengeToggleService;

    @MessageMapping("/matching/join")
    public void handleJoin(Principal principal) {
        long memberId = getAuthenticatedMemberId(principal);
        if (sendDisabledMessage(memberId)) return;
        matchingQueueService.enqueue(memberId);
        messagingTemplate.convertAndSend(
                MATCHING_CHANNEL_PREFIX + memberId,
                MatchingMessageResponse.from(MatchStatusType.MATCHING)
        );
    }

    @MessageMapping("/matching/cancel")
    public void handleCancel(Principal principal) {
        long memberId = getAuthenticatedMemberId(principal);
        matchingQueueService.dequeue(memberId);
        messagingTemplate.convertAndSend(
                MATCHING_CHANNEL_PREFIX + memberId,
                MatchingMessageResponse.from(MatchStatusType.CANCELLED)
        );
    }

    @MessageMapping("/challenge/answer")
    public void handleAnswer(Principal principal, ChallengeMessageRequest message) {
        long memberId = getAuthenticatedMemberId(principal);
        String channel = CHALLENGE_CHANNEL_PREFIX + message.getSessionId();
        if (sendDisabledMessage(channel)) return;

        message.setSenderId(String.valueOf(memberId));
        challengeGameService.submitAnswer(message);
    }

    @MessageMapping("/challenge/chat")
    public void handleChat(Principal principal, ChatMessageRequest message) {
        long memberId = getAuthenticatedMemberId(principal);
        String channel = CHALLENGE_CHANNEL_PREFIX + message.getSessionId();
        if (sendDisabledMessage(channel)) return;

        message.setSenderId(String.valueOf(memberId));
        challengeGameService.receiveChat(message);
    }


    private long getAuthenticatedMemberId(Principal principal) {
        if (!(principal instanceof Authentication auth)) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        Object principalObj = auth.getPrincipal();
        if (!(principalObj instanceof CustomMemberDetails details)) {
            throw new GlobalException(ErrorCode.UNAUTHORIZED);
        }
        return details.getId();
    }

    private boolean sendDisabledMessage(long memberId) {
        return sendDisabledMessage(MATCHING_CHANNEL_PREFIX + memberId);
    }

    private boolean sendDisabledMessage(String channel) {
        if (!challengeToggleService.getCurrentStatus()) {
            messagingTemplate.convertAndSend(
                    channel,
                    MatchingMessageResponse.disabled("챌린지 기능이 현재 비활성화되어 있습니다.")
            );
            return true;
        }
        return false;
    }
}