package com.cmdlee.quizsushi.quiz.challenge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeConnectionRedisServiceTest {
    private static final String SESSION_KEY_PREFIX = "challenge-session:connected:";
    private static final String MEMBER_KEY_PREFIX = "challenge-session:member:";

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private SetOperations<String, String> setOperations;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ChallengeConnectionRedisService challengeConnectionRedisService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @DisplayName("연결을 성공적으로 등록한다")
    @Test
    void register_success() {
        // Given
        String sessionId = "testSessionId";
        String memberId = "testMemberId";

        // When
        challengeConnectionRedisService.register(sessionId, memberId);

        // Then
        verify(setOperations, times(1)).add(eq(SESSION_KEY_PREFIX + sessionId), eq(memberId));
        verify(valueOperations, times(1)).set(eq(MEMBER_KEY_PREFIX + memberId), eq(sessionId));
    }

    @DisplayName("연결을 성공적으로 해제한다")
    @Test
    void unregister_success() {
        // Given
        String sessionId = "testSessionId";
        String memberId = "testMemberId";
        when(valueOperations.get(MEMBER_KEY_PREFIX + memberId)).thenReturn(sessionId);

        // When
        challengeConnectionRedisService.unregister(memberId);

        // Then
        verify(setOperations, times(1)).remove(eq(SESSION_KEY_PREFIX + sessionId), eq(memberId));
        verify(redisTemplate, times(1)).delete(eq(MEMBER_KEY_PREFIX + memberId));
    }
}
