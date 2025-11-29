package com.cmdlee.quizsushi.quiz.challenge.service;

import com.cmdlee.quizsushi.admin.domain.model.AdminMember;
import com.cmdlee.quizsushi.admin.repository.AdminMemberRepository;
import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeToggle;
import com.cmdlee.quizsushi.quiz.challenge.repository.ChallengeToggleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeToggleServiceTest {

    @Mock
    private ChallengeToggleRepository challengeToggleRepository;

    @Mock
    private AdminMemberRepository adminMemberRepository;

    @InjectMocks
    private ChallengeToggleService challengeToggleService;

    private AdminMember testAdmin;

    @BeforeEach
    void setUp() {
        testAdmin = AdminMember.builder().id(1L).username("admin").build();
        when(challengeToggleRepository.findTopByOrderByUpdatedAtDesc()).thenReturn(Optional.empty());
        lenient().when(adminMemberRepository.findById(anyLong())).thenReturn(Optional.of(testAdmin));
    }

    @DisplayName("초기 상태가 false로 로드되는지 테스트")
    @Test
    void initToggleStatus_NoExistingToggle_LoadsFalse() {
        challengeToggleService.initToggleStatus();
        assertThat(challengeToggleService.getCurrentStatus()).isFalse();
    }

    @DisplayName("기존 토글 상태가 true일 때 초기 상태가 true로 로드되는지 테스트")
    @Test
    void initToggleStatus_ExistingToggleTrue_LoadsTrue() {
        // Given
        when(challengeToggleRepository.findTopByOrderByUpdatedAtDesc())
                .thenReturn(Optional.of(ChallengeToggle.of(true, testAdmin)));

        // When
        challengeToggleService.initToggleStatus();

        // Then
        assertThat(challengeToggleService.getCurrentStatus()).isTrue();
    }

    @DisplayName("챌린지 토글 상태를 변경하는지 테스트 (false -> true)")
    @Test
    void toggleChallenge_FromFalseToTrue() {
        challengeToggleService.initToggleStatus();
        // Given
        when(adminMemberRepository.findById(testAdmin.getId())).thenReturn(Optional.of(testAdmin));
        when(challengeToggleRepository.save(any(ChallengeToggle.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        challengeToggleService.toggleChallenge(testAdmin.getId());

        // Then
        assertThat(challengeToggleService.getCurrentStatus()).isTrue();
        verify(challengeToggleRepository, times(1)).save(any(ChallengeToggle.class));
    }

    @DisplayName("챌린지 토글 상태를 변경하는지 테스트 (true -> false)")
    @Test
    void toggleChallenge_FromTrueToFalse() {
        // Given
        when(challengeToggleRepository.findTopByOrderByUpdatedAtDesc())
                .thenReturn(Optional.of(ChallengeToggle.of(true, testAdmin)));

        challengeToggleService.initToggleStatus();

        when(adminMemberRepository.findById(testAdmin.getId()))
                .thenReturn(Optional.of(testAdmin));
        when(challengeToggleRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        challengeToggleService.toggleChallenge(testAdmin.getId());

        // Then
        assertThat(challengeToggleService.getCurrentStatus()).isFalse();
    }

    @DisplayName("관리자 ID가 유효하지 않을 때 GlobalException을 던지는지 테스트")
    @Test
    void toggleChallenge_InvalidAdminId_ThrowsGlobalException() {
        challengeToggleService.initToggleStatus();
        // Given
        when(adminMemberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> challengeToggleService.toggleChallenge(999L))
                .isInstanceOf(GlobalException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ENTITY_NOT_FOUND);
    }
}
