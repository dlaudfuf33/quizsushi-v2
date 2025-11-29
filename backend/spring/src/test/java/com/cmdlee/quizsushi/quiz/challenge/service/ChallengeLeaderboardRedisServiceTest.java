package com.cmdlee.quizsushi.quiz.challenge.service;

import com.cmdlee.quizsushi.quiz.challenge.model.LeaderboardEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeLeaderboardRedisServiceTest {
    private static final String LEADERBOARD_KEY = "leaderboard";
    private static final String LEADERBOARD_HASH_KEY = "leaderboard:players";

    @Mock
    private RedisTemplate<String, String> stringRedisTemplate;

    @Mock
    private RedisTemplate<String, LeaderboardEntry> leaderboardRedisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private ChallengeLeaderboardRedisService challengeLeaderboardRedisService;

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        lenient().doReturn(hashOperations)
                .when(leaderboardRedisTemplate).opsForHash();

        challengeLeaderboardRedisService = new ChallengeLeaderboardRedisService(
                stringRedisTemplate,
                leaderboardRedisTemplate,
                messagingTemplate
        );
    }

    @DisplayName("상위 플레이어를 올바르게 조회한다")
    @Test
    void getTopPlayers_returnsCorrectPlayers() {
        // Given
        Set<String> topMemberIds = new LinkedHashSet<>(List.of("member2", "member1"));
        LeaderboardEntry entry1 = new LeaderboardEntry("member1", "User1", "avatar1.png", 200);
        LeaderboardEntry entry2 = new LeaderboardEntry("member2", "User2", "avatar2.png", 300);

        when(zSetOperations.reverseRange("leaderboard", 0, 9)).thenReturn(topMemberIds);
        when(hashOperations.get(eq("leaderboard:players"), eq("member1"))).thenReturn(entry1);
        when(hashOperations.get(eq("leaderboard:players"), eq("member2"))).thenReturn(entry2);

        // When
        List<LeaderboardEntry> topPlayers = challengeLeaderboardRedisService.getTopPlayers(10);

        // Then
        assertThat(topPlayers).hasSize(2);
        assertThat(topPlayers).extracting("memberId").containsExactly("member2", "member1");
    }


    @DisplayName("리더보드에 플레이어가 없을 때 빈 리스트를 반환한다")
    @Test
    void getTopPlayers_noPlayers_returnsEmptyList() {
        // Given
        when(zSetOperations.reverseRange(LEADERBOARD_KEY, 0, 9L)).thenReturn(Collections.emptySet());

        // When
        List<LeaderboardEntry> topPlayers = challengeLeaderboardRedisService.getTopPlayers(10);

        // Then
        assertThat(topPlayers).isEmpty();
    }

    @DisplayName("점수를 업데이트하고 리더보드를 갱신한다 (새로운 최고 점수)")
    @Test
    void updateScore_newBestScore_updatesAndBroadcasts() {
        // Given
        LeaderboardEntry entry = new LeaderboardEntry("member1", "User1", "avatar1.png");
        long newScore = 150;

        when(zSetOperations.score(LEADERBOARD_KEY, "member1")).thenReturn(100.0);
        when(zSetOperations.add(LEADERBOARD_KEY, "member1", newScore)).thenReturn(true);

        // When
        challengeLeaderboardRedisService.updateScore(entry, newScore);

        // Then
        assertThat(entry.getBestScore()).isEqualTo(newScore);
        verify(zSetOperations, times(1)).add(LEADERBOARD_KEY, "member1", newScore);
        verify(hashOperations, times(1)).put(eq(LEADERBOARD_HASH_KEY), eq("member1"), eq(entry));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/leaderboard"), anyList());
    }

    @DisplayName("리더보드 업데이트를 브로드캐스트한다")
    @Test
    void broadcastLeaderboardUpdate_sendsUpdate() {
        // Given
        when(zSetOperations.reverseRange(LEADERBOARD_KEY, 0, 9L)).thenReturn(Collections.emptySet());

        // When
        challengeLeaderboardRedisService.broadcastLeaderboardUpdate();

        // Then
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/leaderboard"), anyList());
    }
}
