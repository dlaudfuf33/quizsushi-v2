package com.cmdlee.quizsushi.quiz.challenge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchingQueueServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ChallengeGameService challengeGameService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private MatchingQueueService matchingQueueService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @DisplayName("매칭 큐에 성공적으로 추가된다")
    @Test
    void enqueue_success() {
        // Given
        long memberId = 1L;
        when(zSetOperations.score(anyString(), eq(String.valueOf(memberId)))).thenReturn(null);
        when(zSetOperations.add(anyString(), anyString(), anyDouble())).thenReturn(true);

        // When
        matchingQueueService.enqueue(memberId);

        // Then
        verify(zSetOperations, times(1))
                .add(eq("challenge:matching:queue"), eq(String.valueOf(memberId)), anyDouble());
    }

    @DisplayName("이미 매칭 큐에 있는 멤버는 다시 추가되지 않는다")
    @Test
    void enqueue_memberAlreadyExists_doesNothing() {
        // Given
        long memberId = 1L;
        when(zSetOperations.score(anyString(), eq(String.valueOf(memberId)))).thenReturn(100.0);

        // When
        matchingQueueService.enqueue(memberId);

        // Then
        verify(zSetOperations, never()).add(anyString(), anyString(), anyLong());
    }

    @DisplayName("매칭 큐에서 성공적으로 제거된다")
    @Test
    void dequeue_success() {
        // Given
        long memberId = 1L;

        // When
        matchingQueueService.dequeue(memberId);

        // Then
        verify(zSetOperations).remove("challenge:matching:queue", String.valueOf(memberId));
    }

    @DisplayName("매칭 시도 시 조건에 맞는 플레이어가 없으면 아무것도 하지 않는다")
    @Test
    void tryMatch_noPlayers_doesNothing() {
        // Given
        when(zSetOperations.rangeWithScores(anyString(), anyLong(), anyLong())).thenReturn(Collections.emptySet());

        // When
        matchingQueueService.tryMatch();

        // Then
        verify(challengeGameService, never()).createNewSession();
    }
}
