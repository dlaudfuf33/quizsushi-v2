package com.cmdlee.quizsushi.quiz.challenge.service;

import com.cmdlee.quizsushi.quiz.challenge.model.LeaderboardEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ChallengeLeaderboardRedisService {
    private static final String LEADERBOARD_KEY = "leaderboard";
    private static final String LEADERBOARD_HASH_KEY = "leaderboard:players";

    private final RedisTemplate<String, String> stringRedisTemplate;
    private final RedisTemplate<String, LeaderboardEntry> leaderboardRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    public List<LeaderboardEntry> getTopPlayers(int limit) {
        Set<String> topMemberIds = stringRedisTemplate.opsForZSet()
                .reverseRange(LEADERBOARD_KEY, 0, limit - 1L);

        if (topMemberIds == null) return Collections.emptyList();

        List<LeaderboardEntry> topPlayers = new ArrayList<>();

        for (String memberId : topMemberIds) {
            Object raw = leaderboardRedisTemplate.opsForHash().get(LEADERBOARD_HASH_KEY, memberId);
            if (raw instanceof LeaderboardEntry entry) {
                topPlayers.add(entry);
            }
        }
        topPlayers.sort(Comparator.comparingLong(LeaderboardEntry::getBestScore).reversed());
        return topPlayers;
    }

    public void updateScore(LeaderboardEntry entry, long newScore) {
        String memberId = entry.getMemberId();
        Double currentScore = stringRedisTemplate.opsForZSet().score(LEADERBOARD_KEY, memberId);
        boolean isNewRecord = currentScore == null || newScore > currentScore;

        if (isNewRecord) {
            stringRedisTemplate.opsForZSet().add(LEADERBOARD_KEY, memberId, newScore);
        }

        if (isNewRecord) {
            entry.setBestScore(newScore);
            leaderboardRedisTemplate.opsForHash().put(LEADERBOARD_HASH_KEY, memberId, entry);
        }
        broadcastLeaderboardUpdate();
    }

    public void broadcastLeaderboardUpdate() {
        List<LeaderboardEntry> top = getTopPlayers(10);
        messagingTemplate.convertAndSend("/topic/leaderboard", top);
    }
}