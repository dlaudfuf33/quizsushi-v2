package com.cmdlee.quizsushi.quiz.challenge.controller;

import com.cmdlee.quizsushi.global.dto.CommonApiResponse;
import com.cmdlee.quizsushi.quiz.challenge.model.LeaderboardEntry;
import com.cmdlee.quizsushi.quiz.challenge.service.ChallengeLeaderboardRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/challenge/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final ChallengeLeaderboardRedisService challengeLeaderboardRedisService;

    @GetMapping("/top")
    public ResponseEntity<CommonApiResponse<List<LeaderboardEntry>>> getTopPlayers(@RequestParam(defaultValue = "10") int limit) {
        List<LeaderboardEntry> top = challengeLeaderboardRedisService.getTopPlayers(limit);
        return ResponseEntity.ok(CommonApiResponse.ok(top,"리더보드 초기 조회 성공"));
    }

    @MessageMapping("/leaderboard")
    @SendTo("/topic/leaderboard")
    public List<LeaderboardEntry> handleStart(String payload) {
        return challengeLeaderboardRedisService.getTopPlayers(10);
    }
}
