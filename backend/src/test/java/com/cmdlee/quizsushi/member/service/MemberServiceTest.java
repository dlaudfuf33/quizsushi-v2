package com.cmdlee.quizsushi.member.service;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.member.domain.model.OAuthAccount;
import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.member.domain.model.enums.OAuthProvider;
import com.cmdlee.quizsushi.member.dto.OAuthUserInfo;
import com.cmdlee.quizsushi.member.dto.request.UpdateProfileRequest;
import com.cmdlee.quizsushi.member.dto.response.*;
import com.cmdlee.quizsushi.member.repository.MemberRepository;
import com.cmdlee.quizsushi.member.repository.OAuthAccountRepository;
import com.cmdlee.quizsushi.quiz.domain.model.Category;
import com.cmdlee.quizsushi.quiz.domain.model.MemberQuizSolveLog;
import com.cmdlee.quizsushi.quiz.domain.model.Question;
import com.cmdlee.quizsushi.quiz.domain.model.Quiz;
import com.cmdlee.quizsushi.quiz.repository.MemberQuizSolveLogRepository;
import com.cmdlee.quizsushi.quiz.repository.QuizRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private OAuthAccountRepository oauthAccountRepository;

    @Mock
    private MemberQuizSolveLogRepository quizSolveLogRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private OAuthUserInfo oAuthUserInfo;

    @Test
    @DisplayName("기존 OAuth 계정으로 회원 찾기 또는 생성 시 기존 회원을 반환한다")
    void findOrCreateByOAuth_existingMember_returnsMember() {
        // given
        QuizsushiMember mockMember = mock(QuizsushiMember.class);
        when(mockMember.isBan()).thenReturn(false);

        OAuthAccount mockOAuthAccount = mock(OAuthAccount.class);
        when(mockOAuthAccount.getMember()).thenReturn(mockMember);

        // OAuthUserInfo에서 사용하는 값들을 스터빙하여 반환값 설정
        when(oAuthUserInfo.getProvider()).thenReturn(OAuthProvider.GOOGLE);
        when(oAuthUserInfo.getProviderId()).thenReturn("12345");

        when(oauthAccountRepository.findByProviderAndProviderId(any(OAuthProvider.class), anyString()))
                .thenReturn(Optional.of(mockOAuthAccount));

        // when
        QuizsushiMember foundMember = memberService.findOrCreateByOAuth(oAuthUserInfo);

        // then
        assertThat(foundMember).isEqualTo(mockMember);
        verify(oauthAccountRepository, times(1)).findByProviderAndProviderId(oAuthUserInfo.getProvider(), oAuthUserInfo.getProviderId());
        verify(memberRepository, never()).save(any(QuizsushiMember.class));
        verify(oauthAccountRepository, never()).save(any(OAuthAccount.class));
    }

    @Test
    @DisplayName("새로운 OAuth 계정으로 회원 찾기 또는 생성 시 새로운 회원을 생성하고 반환한다")
    void findOrCreateByOAuth_newMember_createsAndReturnsMember() {
        // given
        QuizsushiMember newMemberMock = mock(QuizsushiMember.class);

        OAuthAccount newAccountMock = mock(OAuthAccount.class);

        // findOrCreateByOAuth 내부에서 호출되는 oAuthUserInfo 메서드에 대한 스터빙
        when(oAuthUserInfo.getProvider()).thenReturn(OAuthProvider.GOOGLE);
        when(oAuthUserInfo.getProviderId()).thenReturn("12345");

        when(oauthAccountRepository.findByProviderAndProviderId(any(OAuthProvider.class), anyString()))
                .thenReturn(Optional.empty());
        when(memberRepository.save(any(QuizsushiMember.class))).thenReturn(newMemberMock);
        when(oauthAccountRepository.save(any(OAuthAccount.class))).thenReturn(newAccountMock);

        // when
        QuizsushiMember createdMember = memberService.findOrCreateByOAuth(oAuthUserInfo);

        // then
        assertThat(createdMember.getEmail()).isEqualTo(oAuthUserInfo.getEmail());
        assertThat(createdMember.getNickname()).isEqualTo(oAuthUserInfo.getNickname());

        // 기본 프로필 이미지가 설정되었는지 확인
        assertThat(createdMember.getProfileImage()).isNotNull();
        verify(oauthAccountRepository, times(1)).findByProviderAndProviderId(oAuthUserInfo.getProvider(), oAuthUserInfo.getProviderId());
        verify(memberRepository, times(1)).save(any(QuizsushiMember.class));
        verify(oauthAccountRepository, times(1)).save(any(OAuthAccount.class));
    }

    @Test
    @DisplayName("존재하는 회원 ID로 내 정보 조회 시 MeResponse를 반환한다")
    void getMemberMe_existingMember_returnsMeResponse() {
        // given
        QuizsushiMember mockMember = mock(QuizsushiMember.class);
        when(mockMember.getId()).thenReturn(1L);
        when(mockMember.getEmail()).thenReturn("test@example.com");
        when(mockMember.getNickname()).thenReturn("Test User");

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mockMember));

        // when
        MeResponse meResponse = memberService.getMemberMe(mockMember.getId());

        // then
        assertThat(((MemberMeResponse) meResponse).getEmail()).isEqualTo(mockMember.getEmail());
        assertThat(((MemberMeResponse) meResponse).getNickname()).isEqualTo(mockMember.getNickname());
        verify(memberRepository, times(1)).findById(mockMember.getId());
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 내 정보 조회 시 GlobalException을 발생시킨다")
    void getMemberMe_nonExistingMember_throwsException() {
        // given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        GlobalException exception = assertThrows(GlobalException.class, () -> memberService.getMemberMe(999L));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        verify(memberRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("존재하는 회원 ID로 프로필 조회 시 MemberProfileResponse를 반환한다")
    void getProfile_existingMember_returnsMemberProfileResponse() {
        // given
        QuizsushiMember mockMember = mock(QuizsushiMember.class);
        when(mockMember.getId()).thenReturn(1L);
        when(mockMember.getEmail()).thenReturn("test@example.com");
        when(mockMember.getNickname()).thenReturn("Test User");
        when(mockMember.getProfileImage()).thenReturn("default.webp");

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mockMember));
        when(quizSolveLogRepository.findByMember(any(QuizsushiMember.class))).thenReturn(List.of(
                MemberQuizSolveLog.builder().score(80).build(),
                MemberQuizSolveLog.builder().score(90).build()
        ));
        when(quizRepository.countByAuthorId(anyLong())).thenReturn(5);

        // when
        MemberProfileResponse profileResponse = memberService.getProfile(mockMember.getId());

        // then
        assertThat(profileResponse.getNickname()).isEqualTo(mockMember.getNickname());
        assertThat(profileResponse.getStats().getTotalQuizzesSolved()).isEqualTo(2);
        assertThat(profileResponse.getStats().getAverageScore()).isEqualTo(85.0);
        assertThat(profileResponse.getStats().getTotalQuizzesCreated()).isEqualTo(5);
        verify(memberRepository, times(1)).findById(mockMember.getId());
        verify(quizSolveLogRepository, times(1)).findByMember(mockMember);
        verify(quizRepository, times(1)).countByAuthorId(mockMember.getId());
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 프로필 조회 시 GlobalException을 발생시킨다")
    void getProfile_nonExistingMember_throwsException() {
        // given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        GlobalException exception = assertThrows(GlobalException.class, () -> memberService.getProfile(999L));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        verify(memberRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("풀었던 퀴즈 목록 조회 시 SolvedQuizPageResponse를 반환한다")
    void getSolvedQuizzes_returnsSolvedQuizPageResponse() {
        // given
        QuizsushiMember mockMember = mock(QuizsushiMember.class);
        when(mockMember.getId()).thenReturn(1L);

        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending());
        List<MemberQuizSolveLog> logs = List.of(
                MemberQuizSolveLog.builder().score(100).build(),
                MemberQuizSolveLog.builder().score(90).build()
        );
        PageImpl<MemberQuizSolveLog> logPage = new PageImpl<>(logs, pageable, logs.size());

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mockMember));
        when(quizSolveLogRepository.findByMember(any(QuizsushiMember.class), any(Pageable.class)))
                .thenReturn(logPage);

        // when
        SolvedQuizPageResponse response = memberService.getSolvedQuizzes(mockMember.getId(), page, size);

        // then
        assertThat(response.getQuizzes()).hasSize(2);
        assertThat(response.getCurrentPage()).isEqualTo(page);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getTotalElements()).isEqualTo(2);
        verify(memberRepository, times(1)).findById(mockMember.getId());
        verify(quizSolveLogRepository, times(1)).findByMember(mockMember, pageable);
    }

    @Test
    @DisplayName("생성한 퀴즈 목록 조회 시 CreatedQuizPageResponse를 반환한다")
    void getCreatedQuizzes_returnsCreatedQuizPageResponse() {
        // given
        QuizsushiMember mockMember = mock(QuizsushiMember.class);
        when(mockMember.getId()).thenReturn(1L);

        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Quiz quiz = mock(Quiz.class);
        List<Quiz> quizzes = List.of(quiz);

        when(quiz.getId()).thenReturn(1L);
        when(quiz.getTitle()).thenReturn("Quiz 1");
        when(quiz.getCategory()).thenReturn(mock(Category.class));
        when(quiz.getRating()).thenReturn(4.5);
        when(quiz.getSolveCount()).thenReturn(50L);
        when(quiz.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(quiz.getQuestions()).thenReturn(List.of(mock(Question.class), mock(Question.class)));
        when(quiz.getCategory().getTitle()).thenReturn("Test Category");
        PageImpl<Quiz> quizPage = new PageImpl<>(quizzes, pageable, quizzes.size());

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mockMember));
        when(quizRepository.findCreatedByAuthor(anyLong(), any(Pageable.class)))
                .thenReturn(quizPage);

        // when
        CreatedQuizPageResponse response = memberService.getCreatedQuizzes(mockMember.getId(), page, size);

        // then
        assertThat(response.getQuizzes()).hasSize(1);
        assertThat(response.getCurrentPage()).isEqualTo(page);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        verify(memberRepository, times(1)).findById(mockMember.getId());
        verify(quizRepository, times(1)).findCreatedByAuthor(mockMember.getId(), pageable);
    }

    @Test
    @DisplayName("프로필 업데이트 요청 시 회원 정보가 업데이트된다")
    void updateProfile_success() {
        // given
        QuizsushiMember mockMember = mock(QuizsushiMember.class);
        long memberId = 1L;
        UpdateProfileRequest request = new UpdateProfileRequest("Updated Nickname", "1995-05-05", "FEMALE");
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        doNothing().when(mockMember).updateProfile(request);

        // when
        memberService.updateProfile(memberId, request);

        // then
        verify(memberRepository, times(1)).findById(memberId);
        verify(mockMember, times(1)).updateProfile(request);
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 프로필 업데이트 시 GlobalException을 발생시킨다")
    void updateProfile_nonExistingMember_throwsException() {
        // given
        UpdateProfileRequest request = new UpdateProfileRequest(null, null, null);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        GlobalException exception = assertThrows(GlobalException.class, () -> memberService.updateProfile(999L, request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        verify(memberRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("회원 탈퇴 요청 시 생성한 퀴즈가 삭제되고 회원이 비활성화된다")
    void deleteMe_success() {
        // given
        QuizsushiMember mockMember = mock(QuizsushiMember.class);
        when(mockMember.getId()).thenReturn(1L);
        when(mockMember.isDeleted()).thenReturn(false);
        doNothing().when(mockMember).deactivate();

        List<Quiz> createdQuizzes = List.of(Quiz.builder().title("Quiz 1").author(mockMember).build());

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mockMember));
        when(quizRepository.findByAuthor(any(QuizsushiMember.class))).thenReturn(createdQuizzes);
        doNothing().when(quizRepository).deleteAll(anyList());

        // when
        memberService.deleteMe(mockMember.getId());

        // then
        verify(memberRepository, times(1)).findById(mockMember.getId());
        verify(mockMember, times(1)).isDeleted();
        verify(quizRepository, times(1)).findByAuthor(mockMember);
        verify(quizRepository, times(1)).deleteAll(createdQuizzes);
        verify(mockMember, times(1)).deactivate();
    }

    @Test
    @DisplayName("이미 탈퇴된 회원 탈퇴 요청 시 GlobalException을 발생시킨다")
    void deleteMe_alreadyDeletedMember_throwsException() {
        // given
        QuizsushiMember mockMember = mock(QuizsushiMember.class);
        when(mockMember.getId()).thenReturn(1L);
        when(mockMember.isDeleted()).thenReturn(true);

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(mockMember));

        // when & then
        Long memberId = mockMember.getId();
        GlobalException exception = assertThrows(GlobalException.class, () -> memberService.deleteMe(memberId));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.MEMBER_ALREADY_DELETED);
        verify(memberRepository, times(1)).findById(mockMember.getId());
        verify(mockMember, times(1)).isDeleted();
        verify(quizRepository, never()).findByAuthor(any(QuizsushiMember.class));
        verify(quizRepository, never()).deleteAll(anyList());
        verify(mockMember, never()).deactivate();
    }

    @Test
    @DisplayName("존재하지 않는 회원 탈퇴 요청 시 GlobalException을 발생시킨다")
    void deleteMe_nonExistingMember_throwsException() {
        // given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        GlobalException exception = assertThrows(GlobalException.class, () -> memberService.deleteMe(999L));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ENTITY_NOT_FOUND);
        verify(memberRepository, times(1)).findById(999L);
    }
}
