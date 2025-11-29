package com.cmdlee.quizsushi.quiz.challenge.redis;

import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ChallengeRedisService {

    private final RedisTemplate<String, ChallengeSession> redisTemplate;
    private static final String PREFIX = "challenge-session:";

    public void saveSession(ChallengeSession session) {
        redisTemplate.opsForValue().set(PREFIX + session.getSessionId(), session, Duration.ofMinutes(30));
    }

    public ChallengeSession getSession(String sessionId) {
        return redisTemplate.opsForValue().get(PREFIX + sessionId);
    }

    public void deleteSession(String sessionId) {
        redisTemplate.delete(PREFIX + sessionId);
    }

    public boolean exists(String sessionId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + sessionId));
    }
}