package com.cmdlee.quizsushi.quiz.challenge.service;

import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.challenge.dto.response.MatchingMessageResponse;
import com.cmdlee.quizsushi.quiz.challenge.model.enums.MatchStatusType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingQueueService {
    private static final String MATCHING_QUEUE_KEY = "challenge:matching:queue";
    private static final int MAX_PLAYERS = 5;

    private final RedisTemplate<String, String> redisTemplate;
    private final ChallengeGameService challengeGameService;
    private final SimpMessagingTemplate messagingTemplate;

    public void enqueue(long memberId) {
        String key = String.valueOf(memberId);
        Boolean exists = redisTemplate.opsForZSet().score(MATCHING_QUEUE_KEY, key) != null;
        if (Boolean.TRUE.equals(exists)) return;

        redisTemplate.opsForZSet().add(MATCHING_QUEUE_KEY, key, Instant.now().getEpochSecond());
    }

    @Scheduled(fixedRate = 3000)
    public void tryMatch() {
        long now = Instant.now().getEpochSecond();
        Set<ZSetOperations.TypedTuple<String>> matchingPool = redisTemplate.opsForZSet()
                .rangeWithScores(MATCHING_QUEUE_KEY, 0, -1);

        if (matchingPool == null || matchingPool.isEmpty()) return;

        List<String> matched = new ArrayList<>();
        long firstWaited = -1L;
        boolean has30SecWaiter = false;

        for (ZSetOperations.TypedTuple<String> entry : matchingPool) {
            String memberId = entry.getValue();
            messagingTemplate.convertAndSend("/topic/matching/" + memberId,
                    MatchingMessageResponse.from(MatchStatusType.MATCHING));

            Double score = entry.getScore();
            if (memberId == null || score == null) continue;

            long waitedSec = now - score.longValue();
            if (firstWaited == -1) {
                firstWaited = waitedSec;
            }
            if (waitedSec >= 3) {
                has30SecWaiter = true;
            }
            matched.add(memberId);
        }

        boolean canMatchByGroup = matched.size() >= 3 && firstWaited >= 5;
        boolean canMatchByTimeout = has30SecWaiter;

        if (canMatchByGroup || canMatchByTimeout) {
            List<String> toBeMatched = matched.subList(0, Math.min(matched.size(), MAX_PLAYERS));
            toBeMatched.forEach(id -> redisTemplate.opsForZSet().remove(MATCHING_QUEUE_KEY, id));
            try {
                createSession(toBeMatched);
            } catch (GlobalException e) {
                for (String id : toBeMatched) {
                    long memberId = Long.parseLong(id);
                    enqueue(memberId);
                    messagingTemplate.convertAndSend("/topic/matching/" + memberId,
                            MatchingMessageResponse.of(MatchStatusType.MATCHING, null, "현재 세션이 꽉 찼습니다. 대기 중입니다."));
                }
            }
        }
    }

    private void createSession(List<String> memberIds) {
        String sessionId = challengeGameService.createNewSession();
        List<Long> successful = new ArrayList<>();
        for (String id : memberIds) {
            long memberId = Long.parseLong(id);
            try {
                challengeGameService.joinSession(memberId, sessionId);
                messagingTemplate.convertAndSend("/topic/matching/" + memberId,
                        MatchingMessageResponse.of(MatchStatusType.MATCHED, sessionId));
                successful.add(memberId);
            } catch (GlobalException e) {
                log.warn("session join fail: memberId={}, reason={}", memberId, e.getMessage());
                enqueue(memberId);
            }
        }
        if (successful.isEmpty()) {
            log.warn("No players joined. Session cancelled: {}", sessionId);
            challengeGameService.deleteSession(sessionId);
            return;
        }

        CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS).execute(() ->
                challengeGameService.startMatchedSession(sessionId)
        );
    }

    public void dequeue(long memberId) {
        redisTemplate.opsForZSet().remove(MATCHING_QUEUE_KEY, String.valueOf(memberId));
    }
}