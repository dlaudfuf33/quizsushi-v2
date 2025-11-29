package com.cmdlee.quizsushi.quiz.challenge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChallengeConnectionRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    private String getSessionKey(String sessionId) {
        return "challenge-session:connected:" + sessionId;
    }

    private String getMemberKey(String memberId) {
        return "challenge-session:member:" + memberId;
    }

    public void register(String sessionId, String memberId) {
        redisTemplate.opsForSet().add(getSessionKey(sessionId), memberId);
        redisTemplate.opsForValue().set(getMemberKey(memberId), sessionId);
    }

    public void unregister(String memberId) {
        String sessionId = redisTemplate.opsForValue().get(getMemberKey(memberId));
        if (sessionId != null) {
            redisTemplate.opsForSet().remove(getSessionKey(sessionId), memberId);
        }
        redisTemplate.delete(getMemberKey(memberId));
    }

    public boolean isOnline(String sessionId, String memberId) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(getSessionKey(sessionId), memberId)
        );
    }

    public long getOnlineCount(String sessionId) {
        Long count = redisTemplate.opsForSet().size(getSessionKey(sessionId));
        return count != null ? count : 0;
    }

    public String getSessionIdOf(String memberId) {
        return redisTemplate.opsForValue().get(getMemberKey(memberId));
    }
}