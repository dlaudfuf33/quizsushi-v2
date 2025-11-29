package com.cmdlee.quizsushi.quiz.challenge.model;

import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.quiz.challenge.dto.request.ChatMessageRequest;
import com.cmdlee.quizsushi.quiz.challenge.model.dto.BroadcastMessage;
import com.cmdlee.quizsushi.quiz.challenge.model.dto.PlayerAnswer;
import com.cmdlee.quizsushi.quiz.challenge.model.enums.ChallengePhaseType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
@RedisHash("challengeSession")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChallengeSession implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final int MAX_LOG_SIZE = 30;

    @Id
    private String sessionId;

    // 세션 상태
    private ChallengePhaseType phase = ChallengePhaseType.WAITING;

    // 유저 상태
    private Map<String, PlayerStatus> players = new ConcurrentHashMap<>();
    private int maxPlayers = 5;

    // 퀴즈 상태
    private int currentRound = 0;
    private ChallengeQuiz currentQuiz;

    // 로그
    private List<BroadcastMessage> broadcastLog = Collections.synchronizedList(new ArrayList<>());
    private List<ChatMessageRequest> chatLog = Collections.synchronizedList(new ArrayList<>());

    public void setPhase(ChallengePhaseType phase) {
        this.phase = phase;

        switch (phase) {
            case START -> addBroadcast("🚀 게임이 시작되었습니다!");
            case PLAYING -> addBroadcast("📝 문제 풀이가 시작되었습니다! 정답을 입력해 주세요.");
            case GRADING -> addBroadcast("🧠 제출된 답안을 채점 중입니다...");
            case GENERATING -> addBroadcast("🤖 다음 문제를 준비하고 있어요...");
            case GAMEOVER -> addBroadcast("🏁 게임이 종료되었습니다. 모두 수고하셨습니다!");
        }
    }

    public ChallengeSession(String sessionId) {
        this.sessionId = sessionId;
        addBroadcast("🔥 퀴즈 세션이 생성되었습니다.");
    }

    public boolean tryJoin(QuizsushiMember member, int basicHP) {
        if (!isJoinable()) return false;
        players.put(String.valueOf(member.getId()), new PlayerStatus(member, basicHP));
        addBroadcast("👤 " + member.getNickname() + " 님이 참여했습니다.");
        return true;
    }

    public void eliminatePlayer(String memberId) {
        PlayerStatus player = players.get(memberId);
        if (player == null || player.isDead()) return;
        player.forceEliminate();
        addBroadcast("💀 " + player.getNickname() + " 님이 탈락했습니다.");
    }

    public void startGame() {
        if (phase == ChallengePhaseType.WAITING && !players.isEmpty()) {
            setPhase(ChallengePhaseType.PLAYING);
        }
    }

    public void setCurrentQuestion(ChallengeQuiz challengeQuiz) {
        this.currentQuiz = challengeQuiz;
        this.currentRound++;
        addBroadcast("📢 새 문제가 출제되었습니다.");
    }

    public void receiveChat(String memberId, String text) {
        PlayerStatus sender = players.get(memberId);
        if (sender == null) return;
        if (chatLog.size() >= MAX_LOG_SIZE) chatLog.remove(0);
        chatLog.add(new ChatMessageRequest(sender.getNickname(), text));
    }

    public boolean hasAllAnswered() {
        return players.values().stream()
                .filter(p -> !p.isDead())
                .allMatch(PlayerStatus::isAnswered);
    }

    public void submitPlayerAnswer(String memberId, String answer) {
        PlayerStatus player = players.get(memberId);
        if (player == null || player.isDead()) return;
        player.submitAnswer(answer);
        addBroadcast("\uD83D\uDE4B" + player.getNickname() + "님이 정답을 제출하셨습니다!");
    }

    @JsonIgnore
    public List<PlayerAnswer> getPlayersAnswer() {
        return players.values().stream()
                .filter(p -> !p.isDead())
                .map(p -> new PlayerAnswer(p.getMemberId(), p.getNickname(), p.getAvatar(), p.getSubmittedAnswer()))
                .toList();
    }

    public long processAnswerResult(String memberId, boolean correct, int baseScore) {
        PlayerStatus player = players.get(memberId);
        if (player == null || player.isDead()) return 0;

        long updatedScore;
        if (correct) {
            updatedScore = player.increaseScoreWithCombo(baseScore);
            addBroadcast("✅ " + player.getNickname() + " 정답! (+" + updatedScore + "점)");
        } else {
            player.wrongAnswer();
            addBroadcast("❌ " + player.getNickname() + " 오답!");
            updatedScore = player.getScore();
            if (player.isDead()) {
                addBroadcast("💀 " + player.getNickname() + " 님이 탈락했습니다.");
            }
        }
        player.updateActivityTime();
        return updatedScore;
    }

    public void resetPlayersAnswer() {
        players.values().forEach(PlayerStatus::resetAnswerState);
    }

    public boolean isPlayerDead(String memberId) {
        return getPlayer(memberId).isDead();
    }

    public PlayerStatus getPlayer(String memberId) {
        return players.get(memberId);
    }

    public void sessionOver() {
        setPhase(ChallengePhaseType.GAMEOVER);
    }

    @JsonIgnore
    public boolean isJoinable() {
        return phase == ChallengePhaseType.WAITING && players.size() < maxPlayers;
    }

    @JsonIgnore
    public boolean isEveryoneDead() {
        return players.values().stream().allMatch(PlayerStatus::isDead);
    }

    @JsonIgnore
    public boolean isGameOver() {
        return isEveryoneDead();
    }

    @JsonIgnore
    public boolean isTimedOut() {
        return currentQuiz != null && currentQuiz.getGivenAt() != null &&
                currentQuiz.getLimitTime() > 0 &&
                Instant.now().isAfter(currentQuiz.getDeadlineTime());
    }

    public void addBroadcast(String message) {
        synchronized (broadcastLog) {
            if (broadcastLog.size() >= MAX_LOG_SIZE) {
                broadcastLog.remove(0);
            }
            broadcastLog.add(new BroadcastMessage(message));
        }
    }
}