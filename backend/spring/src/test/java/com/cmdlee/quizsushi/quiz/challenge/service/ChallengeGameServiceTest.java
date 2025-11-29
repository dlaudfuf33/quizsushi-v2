package com.cmdlee.quizsushi.quiz.challenge.service;

import com.cmdlee.quizsushi.ai.service.GeminiAiService;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.member.service.MemberService;
import com.cmdlee.quizsushi.quiz.challenge.dto.request.ChallengeMessageRequest;
import com.cmdlee.quizsushi.quiz.challenge.dto.request.ChatMessageRequest;
import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeQuiz;
import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeSession;
import com.cmdlee.quizsushi.quiz.challenge.model.LeaderboardEntry;
import com.cmdlee.quizsushi.quiz.challenge.model.PlayerStatus;
import com.cmdlee.quizsushi.quiz.challenge.model.enums.ChallengePhaseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeGameServiceTest {

    @Mock
    private ChallengeSessionRedisService challengeSessionRedisService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private MemberService memberService;
    @Mock
    private ChallengeLeaderboardRedisService challengeLeaderboardRedisService;
    @Mock
    private GeminiAiService geminiAiService;
    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    @InjectMocks
    private ChallengeGameService challengeGameService;

    private QuizsushiMember testMember;
    private ChallengeSession testSession;
    private ChallengeQuiz testQuiz;

    @BeforeEach
    void setUp() {
        testMember = QuizsushiMember.builder().id(1L).nickname("testUser").profileImage("avatar.png").build();
        testSession = new ChallengeSession("sessionId123");
        testQuiz = ChallengeQuiz.of("Question", List.of("Answer"), 60, "Explain", 100);

        try {
            java.lang.reflect.Field field = ChallengeGameService.class.getDeclaredField("scheduledExecutorService");
            field.setAccessible(true);
            field.set(challengeGameService, scheduledExecutorService);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @DisplayName("새로운 세션을 성공적으로 생성한다")
    @Test
    void createNewSession_success() {
        // Given
        when(challengeSessionRedisService.createNewSession()).thenReturn("newSessionId");

        // When
        String sessionId = challengeGameService.createNewSession();

        // Then
        assertThat(sessionId).isEqualTo("newSessionId");
        verify(challengeSessionRedisService, times(1)).createNewSession();
    }

    @DisplayName("세션에 성공적으로 참여한다")
    @Test
    void joinSession_success() {
        // Given
        when(challengeSessionRedisService.getSession(anyString())).thenReturn(testSession);
        when(memberService.findMemberById(anyLong())).thenReturn(testMember);
        testSession.setPhase(ChallengePhaseType.WAITING);

        // When
        challengeGameService.joinSession(testMember.getId(), testSession.getSessionId());

        // Then
        assertThat(testSession.getPlayers()).containsKey(String.valueOf(testMember.getId()));
        verify(challengeSessionRedisService, times(1)).saveSession(testSession);
        verify(messagingTemplate, times(1))
                .convertAndSend(any(String.class), any(Object.class));

    }

    @DisplayName("존재하지 않는 세션에 참여 시 GlobalException을 던진다")
    @Test
    void joinSession_sessionNotFound_throwsException() {
        // Given
        when(challengeSessionRedisService.getSession(anyString())).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> challengeGameService.joinSession(testMember.getId(), "nonExistentSession"))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SESSION_NOT_FOUND);
    }

    @DisplayName("이미 참여한 멤버가 세션에 다시 참여 시 아무것도 하지 않는다")
    @Test
    void joinSession_memberAlreadyJoined_doesNothing() {
        // Given
        testSession.tryJoin(testMember, 3);
        when(challengeSessionRedisService.getSession(anyString())).thenReturn(testSession);
        when(memberService.findMemberById(anyLong())).thenReturn(testMember);

        // When
        challengeGameService.joinSession(testMember.getId(), testSession.getSessionId());

        // Then
        verify(challengeSessionRedisService, never()).saveSession(any());
        verify(messagingTemplate, never())
                .convertAndSend(any(String.class), any(Object.class));
    }

    @DisplayName("세션이 참여 불가능한 상태일 때 GlobalException을 던진다")
    @Test
    void joinSession_sessionNotJoinable_throwsException() {
        // Given
        testSession.setPhase(ChallengePhaseType.PLAYING);
        when(challengeSessionRedisService.getSession(anyString())).thenReturn(testSession);
        when(memberService.findMemberById(anyLong())).thenReturn(testMember);

        // When & Then
        // When & Then
        Long memberId = testMember.getId();
        String sessionId = testSession.getSessionId();

        assertThatThrownBy(() -> challengeGameService.joinSession(memberId, sessionId))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED_SESSION);
    }

    @DisplayName("매칭된 세션을 성공적으로 시작한다")
    @Test
    void startMatchedSession_success() {
        // Given
        testSession.tryJoin(testMember, 3);
        when(challengeSessionRedisService.getSession(anyString())).thenReturn(testSession);
        when(geminiAiService.generateChallengeQuizWithGemini(anyInt())).thenReturn(testQuiz);

        // When
        challengeGameService.startMatchedSession(testSession.getSessionId());

        // Then
        assertThat(testSession.getPhase()).isEqualTo(ChallengePhaseType.PLAYING);
        assertThat(testSession.getCurrentQuiz()).isEqualTo(testQuiz);
        verify(challengeSessionRedisService, times(3)).saveSession(testSession);
        verify(messagingTemplate, atLeast(2)).convertAndSend(any(String.class), any(Object.class));
        verify(scheduledExecutorService, times(1)).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @DisplayName("답변을 성공적으로 제출한다")
    @Test
    void submitAnswer_success() {
        // Given
        testSession.tryJoin(testMember, 3);
        testSession.setCurrentQuestion(testQuiz);
        testSession.setPhase(ChallengePhaseType.PLAYING);
        when(challengeSessionRedisService.getSession(anyString())).thenReturn(testSession);

        ChallengeMessageRequest request = new ChallengeMessageRequest();
        request.setSessionId(testSession.getSessionId());
        request.setSenderId(String.valueOf(testMember.getId()));
        request.setContent("Test Answer");

        // When
        challengeGameService.submitAnswer(request);

        // Then
        PlayerStatus playerStatus = testSession.getPlayers().get(String.valueOf(testMember.getId()));
        assertThat(playerStatus.isAnswered()).isTrue();
        assertThat(playerStatus.getSubmittedAnswer()).isEqualTo("Test Answer");
        verify(challengeSessionRedisService, atLeast(1)).saveSession(testSession);
        verify(messagingTemplate, atLeast(1))
                .convertAndSend(any(String.class), any(Object.class));
    }

    @DisplayName("채팅 메시지를 성공적으로 수신한다")
    @Test
    void receiveChat_success() {
        // Given
        testSession.tryJoin(testMember, 3);
        when(challengeSessionRedisService.getSession(anyString())).thenReturn(testSession);

        ChatMessageRequest chatMessage = new ChatMessageRequest();
        chatMessage.setSessionId(testSession.getSessionId());
        chatMessage.setSenderId(String.valueOf(testMember.getId()));
        chatMessage.setNickname(testMember.getNickname());
        chatMessage.setContent("Hello, world!");

        // When
        challengeGameService.receiveChat(chatMessage);

        // Then
        assertThat(testSession.getChatLog()).hasSize(1);
        assertThat(testSession.getChatLog().get(0).getContent()).isEqualTo("Hello, world!");
        verify(challengeSessionRedisService, times(1)).saveSession(testSession);
        verify(messagingTemplate, times(1))
                .convertAndSend(any(String.class), any(Object.class));
    }

    @DisplayName("세션을 성공적으로 삭제한다")
    @Test
    void deleteSession_success() {
        // Given
        String sessionId = "testSessionId";

        // When
        challengeGameService.deleteSession(sessionId);

        // Then
        verify(challengeSessionRedisService, times(1)).deleteSession(sessionId);
    }

    @DisplayName("정답 처리 시 점수가 올바르게 업데이트되고 리더보드가 갱신된다")
    @Test
    void gradingAnswers_correctAnswer_updatesScoreAndLeaderboard() {
        // Given
        testSession.tryJoin(testMember, 3);
        testSession.setCurrentQuestion(testQuiz);
        testSession.setPhase(ChallengePhaseType.PLAYING);
        testSession.submitPlayerAnswer(String.valueOf(testMember.getId()), "Answer");
        doNothing().when(challengeLeaderboardRedisService).updateScore(any(LeaderboardEntry.class), anyLong());

        // When
        challengeGameService.checkAndGradingAnswers(testSession);

        // Then
        PlayerStatus playerStatus = testSession.getPlayers().get(String.valueOf(testMember.getId()));
        assertThat(playerStatus.getScore()).isEqualTo(testQuiz.getScore());
        verify(challengeLeaderboardRedisService, atLeast(1))
                .updateScore(any(LeaderboardEntry.class), eq((long) testQuiz.getScore()));
        verify(challengeSessionRedisService, atLeast(1)).saveSession(testSession);
        verify(messagingTemplate, atLeast(1))
                .convertAndSend(any(String.class), any(Object.class));
    }

    @DisplayName("오답 처리 시 HP가 감소하고 리더보드가 갱신된다")
    @Test
    void gradingAnswers_wrongAnswer_decreasesHpAndUpdatesLeaderboard() {
        // Given
        testSession.tryJoin(testMember, 3);
        testSession.setCurrentQuestion(testQuiz);
        testSession.setPhase(ChallengePhaseType.PLAYING);
        testSession.submitPlayerAnswer(String.valueOf(testMember.getId()), "Wrong Answer");

        doNothing().when(challengeLeaderboardRedisService).updateScore(any(LeaderboardEntry.class), anyLong());

        // When
        challengeGameService.checkAndGradingAnswers(testSession);

        // Then
        PlayerStatus playerStatus = testSession.getPlayers().get(String.valueOf(testMember.getId()));
        assertThat(playerStatus.getHp()).isEqualTo(2);
        verify(challengeLeaderboardRedisService, atLeast(1)).updateScore(any(LeaderboardEntry.class), eq(0L));
        verify(challengeSessionRedisService, atLeast(1)).saveSession(testSession);
        verify(messagingTemplate, atLeast(1))
                .convertAndSend(any(String.class), any(Object.class));
    }

    @DisplayName("모든 플레이어가 답변했거나 타임아웃 시 채점 로직이 호출된다")
    @Test
    void checkAndGradingAnswers_allAnsweredOrTimedOut_gradingLogicCalled() {
        // Given
        testSession.tryJoin(testMember, 3);
        testSession.setCurrentQuestion(testQuiz);
        testSession.setPhase(ChallengePhaseType.PLAYING);
        testSession.submitPlayerAnswer(String.valueOf(testMember.getId()), "Answer");

        // When
        challengeGameService.checkAndGradingAnswers(testSession);

        // Then
        assertThat(testSession.getPhase()).isEqualTo(ChallengePhaseType.GRADING);
        verify(challengeSessionRedisService, atLeast(1)).saveSession(testSession);
        verify(messagingTemplate, atLeast(1)).convertAndSend(any(String.class), any(Object.class));
    }

    @DisplayName("라운드 타임아웃 시 채점 로직이 호출되고 HP가 줄어든다")
    @Test
    void roundTimeout_triggersGradingAndDecreasesHp() throws Exception {
        // Given
        testSession.tryJoin(testMember, 3);
        testSession.setCurrentQuestion(testQuiz);
        testSession.setPhase(ChallengePhaseType.PLAYING);

        Field deadlineField = testQuiz.getClass().getDeclaredField("deadlineTime");
        deadlineField.setAccessible(true);
        deadlineField.set(testQuiz, Instant.now().minusSeconds(10));

        // When
        challengeGameService.checkAndGradingAnswers(testSession);

        // Then
        PlayerStatus playerStatus = testSession.getPlayers().get(String.valueOf(testMember.getId()));
        assertThat(playerStatus.getHp()).isEqualTo(2);
    }

    @DisplayName("게임 오버 시 세션이 종료되고 삭제된다")
    @Test
    void checkGameOverAndProceed_gameOver_sessionEndsAndDeleted() throws Exception {
        // Given
        testSession.tryJoin(testMember, 0);
        testSession.setPhase(ChallengePhaseType.PLAYING);
        testSession.setCurrentQuestion(testQuiz);

        // 타임아웃 유도
        Field deadlineField = testQuiz.getClass().getDeclaredField("deadlineTime");
        deadlineField.setAccessible(true);
        deadlineField.set(testQuiz, Instant.now().minusSeconds(10));

        when(challengeSessionRedisService.getSession(anyString())).thenReturn(testSession);
        doNothing().when(challengeSessionRedisService).deleteSession(anyString());

        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(scheduledExecutorService).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

        // When
        challengeGameService.checkAndGradingAnswers(testSession);

        // Then
        assertThat(testSession.getPhase()).isEqualTo(ChallengePhaseType.GAMEOVER);
        verify(challengeSessionRedisService, atLeast(1)).deleteSession(testSession.getSessionId());
        verify(messagingTemplate, atLeast(1)).convertAndSend(any(String.class), any(Object.class));
    }
}
