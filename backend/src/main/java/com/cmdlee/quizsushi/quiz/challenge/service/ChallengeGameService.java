package com.cmdlee.quizsushi.quiz.challenge.service;

import com.cmdlee.quizsushi.ai.service.GeminiAiService;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.member.service.MemberService;
import com.cmdlee.quizsushi.quiz.challenge.dto.request.ChallengeMessageRequest;
import com.cmdlee.quizsushi.quiz.challenge.dto.request.ChatMessageRequest;
import com.cmdlee.quizsushi.quiz.challenge.dto.response.ChallengeStatusMessageResponse;
import com.cmdlee.quizsushi.quiz.challenge.dto.response.GameStateResponse;
import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeQuiz;
import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeSession;
import com.cmdlee.quizsushi.quiz.challenge.model.LeaderboardEntry;
import com.cmdlee.quizsushi.quiz.challenge.model.dto.PlayerAnswer;
import com.cmdlee.quizsushi.quiz.challenge.model.enums.ChallengePhaseType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChallengeGameService {

    private final ChallengeSessionRedisService challengeSessionRedisService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MemberService memberService;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(4);
    private final ChallengeLeaderboardRedisService challengeLeaderboardRedisService;
    private final GeminiAiService geminiAiService;

    public String createNewSession() {
        return challengeSessionRedisService.createNewSession();
    }

    public void joinSession(long memberId, String sessionId) {
        ChallengeSession session = challengeSessionRedisService.getSession(sessionId);
        if (session == null) {
            throw new GlobalException(ErrorCode.SESSION_NOT_FOUND);
        }
        QuizsushiMember member = memberService.findMemberById(memberId);
        if (session.getPlayers().containsKey(String.valueOf(member.getId()))) return;

        boolean success = session.tryJoin(member, 3);
        if (!success) {
            throw new GlobalException(ErrorCode.ACCESS_DENIED_SESSION);
        }
        challengeSessionRedisService.saveSession(session);
        sendGameState(session);
    }

    public void startMatchedSession(String sessionId) {
        ChallengeSession session = challengeSessionRedisService.getSession(sessionId);
        if (session == null) {
            throw new GlobalException(ErrorCode.SESSION_NOT_FOUND);
        }
        if (session.getPhase() != ChallengePhaseType.WAITING) {
            sendGameState(session);
            return;
        }
        session.startGame();
        challengeSessionRedisService.saveSession(session);
        sendGameState(session);
        proceedToNextQuestion(session);
    }

    public void proceedToNextQuestion(ChallengeSession session) {
        session.resetPlayersAnswer();

        session.setPhase(ChallengePhaseType.GENERATING);
        challengeSessionRedisService.saveSession(session);
        sendGameState(session);

        ChallengeQuiz next = generateQuiz(session);

        session.setCurrentQuestion(next);
        session.setPhase(ChallengePhaseType.PLAYING);
        challengeSessionRedisService.saveSession(session);
        sendGameState(session);

        scheduleRoundTimeout(session);
    }

    public ChallengeQuiz generateQuiz(ChallengeSession session) {
        return geminiAiService.generateChallengeQuizWithGemini(session.getCurrentRound());
    }

    public void submitAnswer(ChallengeMessageRequest message) {
        String sessionId = message.getSessionId();
        String memberId = message.getSenderId();
        String submittedAnswer = message.getContent();

        ChallengeSession session = challengeSessionRedisService.getSession(sessionId);
        session.submitPlayerAnswer(memberId, submittedAnswer);
        challengeSessionRedisService.saveSession(session);
        sendGameState(session);

        checkAndGradingAnswers(session);
    }


    public void checkAndGradingAnswers(ChallengeSession session) {
        if (session == null || session.getPhase() != ChallengePhaseType.PLAYING) return;
        if (session.hasAllAnswered() || session.isTimedOut()) {
            gradingAnswers(session);
        }
    }

    private void gradingAnswers(ChallengeSession session) {
        session.setPhase(ChallengePhaseType.GRADING);
        challengeSessionRedisService.saveSession(session);
        sendGameState(session);

        ChallengeQuiz currentQuiz = session.getCurrentQuiz();
        int currentScore = currentQuiz.getScore();
        List<String> correctAnswerList = currentQuiz.getAnswerList();


        // 순회->채점
        for (PlayerAnswer playerAnswer : session.getPlayersAnswer()) {
            String memberId = playerAnswer.getMemberId();
            String nickname = playerAnswer.getNickname();
            String avatar = playerAnswer.getAvatar();
            String submittedAnswer = playerAnswer.getSubmittedAnswer();
            boolean isCorrect = correctAnswerList.stream()
                    .anyMatch(correct -> correct.equalsIgnoreCase(submittedAnswer.trim()));

            long updatedScore = session.processAnswerResult(memberId, isCorrect, currentScore);
            challengeLeaderboardRedisService.updateScore(
                    new LeaderboardEntry(memberId, nickname, avatar),
                    updatedScore
            );
        }
        challengeSessionRedisService.saveSession(session);
        sendGameState(session);
        runAfterDelay(session, 10000, () -> checkGameOverAndProceed(session));
    }

    public void runAfterDelay(ChallengeSession session, long delayMillis, Runnable task) {
        String sessionId = session.getSessionId();
        int roundAtSchedule = session.getCurrentRound();

        scheduledExecutorService.schedule(() -> {
            try {
                ChallengeSession latest = challengeSessionRedisService.getSession(sessionId);
                if (latest != null &&
                        latest.getCurrentRound() == roundAtSchedule &&
                        latest.getPhase() == session.getPhase()) {

                    task.run();
                }
            } catch (Exception e) {
                log.error("runAfterDelay failed: {}", e.getMessage(), e);
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    public void scheduleRoundTimeout(ChallengeSession session) {
        Instant deadlineTime = session.getCurrentQuiz().getDeadlineTime();
        long delay = Duration.between(Instant.now(), deadlineTime).toMillis();
        if (delay <= 0) {
            checkAndGradingAnswers(session);
            return;
        }
        String sessionId = session.getSessionId();
        int roundAtSchedule = session.getCurrentRound();

        scheduledExecutorService.schedule(() -> {
            try {
                ChallengeSession latest = challengeSessionRedisService.getSession(sessionId);
                if (latest != null
                        && latest.getPhase() == ChallengePhaseType.PLAYING
                        && latest.getCurrentRound() == roundAtSchedule
                        && latest.isTimedOut()) {

                    checkAndGradingAnswers(session);
                }
            } catch (Exception e) {
                log.error("라운드 자동 평가 실패: {}", e.getMessage());
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    public void receiveChat(ChatMessageRequest message) {
        String sessionId = message.getSessionId();
        String senderId = message.getSenderId();
        String content = message.getContent();

        ChallengeSession session = challengeSessionRedisService.getSession(sessionId);
        if (session == null) return;

        session.receiveChat(senderId, content);
        challengeSessionRedisService.saveSession(session);

        sendGameState(session);
    }

    private void checkGameOverAndProceed(ChallengeSession session) {
        if (session == null) return;
        if (session.isGameOver()) {
            sessionOver(session);
        } else {
            proceedToNextQuestion(session);
        }
    }

    private void sessionOver(ChallengeSession session) {
        session.sessionOver();
        sendGameState(session);

        deleteSession(session.getSessionId());
    }

    public void sendGameState(ChallengeSession session) {
        GameStateResponse gameState = GameStateResponse.from(session);
        ChallengeStatusMessageResponse msg = ChallengeStatusMessageResponse.of(session.getSessionId(), gameState);
        messagingTemplate.convertAndSend("/topic/challenge/" + session.getSessionId(), msg);
    }

    public void deleteSession(String sessionId) {
        challengeSessionRedisService.deleteSession(sessionId);
        log.debug("Session deleted: {}", sessionId);
    }

}