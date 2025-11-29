package com.cmdlee.quizsushi.quiz.challenge.service;

import com.cmdlee.quizsushi.global.config.scheduler.ChallengeProperties;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;
@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeSessionRedisService {
    private static final String SESSION_ID_SET_KEY = "challenge:sessions";
    private static final String SESSION_KEY_PREFIX = "challenge:sessions:";

    private final ChallengeProperties challengeProperties;

    private final RedisTemplate<String, ChallengeSession> challengeSessionRedisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;


    public String createNewSession() {
        String rawId = UUID.randomUUID().toString();
        String sessionKey = SESSION_KEY_PREFIX + rawId;
        assertSessionCreatable(rawId);

        ChallengeSession session = new ChallengeSession(rawId);
        challengeSessionRedisTemplate.opsForValue().set(sessionKey, session, Duration.ofMinutes(10));
        stringRedisTemplate.opsForSet().add(SESSION_ID_SET_KEY, sessionKey);

        return rawId;
    }

    public void saveSession(ChallengeSession session) {
            String sessionKey = SESSION_KEY_PREFIX + session.getSessionId();
        challengeSessionRedisTemplate.opsForValue().set(sessionKey, session, Duration.ofMinutes(10));
        stringRedisTemplate.opsForSet().add(SESSION_ID_SET_KEY, sessionKey);
    }

    public ChallengeSession getSession(String sessionId) {
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        return challengeSessionRedisTemplate.opsForValue().get(sessionKey);
    }

    public void deleteSession(String sessionId) {
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        challengeSessionRedisTemplate.delete(sessionKey);
        stringRedisTemplate.opsForSet().remove(SESSION_ID_SET_KEY, sessionKey);
    }

    public long getActiveSessionCount() {
        Long count = stringRedisTemplate.opsForSet().size(SESSION_ID_SET_KEY);
        return count != null ? count : 0;
    }

    public void assertSessionCreatable(String sessionId) {
        String sessionKey = SESSION_KEY_PREFIX + sessionId;
        Boolean exists = challengeSessionRedisTemplate.hasKey(sessionKey);
        if (Boolean.TRUE.equals(exists)) {
            throw new GlobalException(ErrorCode.SESSION_ALREADY_EXISTS);
        }

        long count = getActiveSessionCount();
        if (count >= challengeProperties.getMaxSessions()) {
            throw new GlobalException(ErrorCode.SESSION_LIMIT_EXCEEDED);
        }
    }

    @Scheduled(fixedDelay = 300_000)
    public void cleanExpiredSessionIds() {
        Set<String> sessionIds = stringRedisTemplate.opsForSet().members(SESSION_ID_SET_KEY);
        if (sessionIds == null) return;

        for (String sessionId : sessionIds) {
            Boolean exists = challengeSessionRedisTemplate.hasKey(sessionId);
            if (exists == null || !exists) {
                stringRedisTemplate.opsForSet().remove(SESSION_ID_SET_KEY, sessionId);
            }
        }
    }
}
