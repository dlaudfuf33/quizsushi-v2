package com.cmdlee.quizsushi.member.service;

import com.cmdlee.quizsushi.global.auth.jwt.JwtTokenProvider;
import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.member.dto.OAuthUserInfo;
import com.cmdlee.quizsushi.member.security.TokenPair;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private KakaoOAuthService kakaoOAuthService;

    @Mock
    private GoogleOAuthService googleOAuthService;

    @Mock
    private MemberService memberService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private OAuthUserInfo oAuthUserInfo;

    @Mock
    private QuizsushiMember member;

    @Test
    @DisplayName("구글 로그인 URL을 생성한다")
    void getGoogleLoginUrl() {
        // given
        String expectedUrl = "https://accounts.google.com/o/oauth2/v2/auth?...";
        when(googleOAuthService.generateGoogleLoginUrl()).thenReturn(expectedUrl);

        // when
        String actualUrl = authService.getGoogleLoginUrl();

        // then
        assertThat(actualUrl).isEqualTo(expectedUrl);
        verify(googleOAuthService, times(1)).generateGoogleLoginUrl();
    }

    @Test
    @DisplayName("구글 로그인 콜백을 처리하고 토큰 쌍을 반환한다")
    void handleGoogleCallback() {
        // given
        String code = "test_code";
        String accessToken = "test_access_token";

        when(googleOAuthService.handleCallback(code)).thenReturn(oAuthUserInfo);
        when(memberService.findOrCreateByOAuth(oAuthUserInfo)).thenReturn(member);
        when(member.getId()).thenReturn(1L);
        when(jwtTokenProvider.createToken(anyString())).thenReturn(accessToken);
        doNothing().when(refreshTokenService).save(anyString(), anyString(), any(HttpServletRequest.class));

        // when
        TokenPair tokenPair = authService.handleGoogleCallback(code, request);

        // then
        assertThat(tokenPair.getAccessToken()).isEqualTo(accessToken);
        assertThat(tokenPair.getRefreshToken()).isNotNull();
        verify(googleOAuthService, times(1)).handleCallback(code);
        verify(memberService, times(1)).findOrCreateByOAuth(oAuthUserInfo);
        verify(refreshTokenService, times(1)).save(anyString(), anyString(), any(HttpServletRequest.class));
        verify(jwtTokenProvider, times(1)).createToken("1");
    }

    @Test
    @DisplayName("카카오 로그인 URL을 생성한다")
    void getKakaoLoginUrl() {
        // given
        String expectedUrl = "https://kauth.kakao.com/oauth/authorize?...";
        when(kakaoOAuthService.generateKakaoLoginUrl()).thenReturn(expectedUrl);

        // when
        String actualUrl = authService.getKakaoLoginUrl();

        // then
        assertThat(actualUrl).isEqualTo(expectedUrl);
        verify(kakaoOAuthService, times(1)).generateKakaoLoginUrl();
    }

    @Test
    @DisplayName("카카오 로그인 콜백을 처리하고 토큰 쌍을 반환한다")
    void handleKakaoCallback() {
        // given
        String code = "test_code";
        String kakaoAccessToken = "kakao_access_token";
        String accessToken = "test_access_token";

        when(kakaoOAuthService.requestAccessToken(code)).thenReturn(kakaoAccessToken);
        when(kakaoOAuthService.getUserInfo(kakaoAccessToken)).thenReturn(oAuthUserInfo);
        when(memberService.findOrCreateByOAuth(oAuthUserInfo)).thenReturn(member);
        when(member.getId()).thenReturn(1L);
        when(jwtTokenProvider.createToken(anyString())).thenReturn(accessToken);
        doNothing().when(refreshTokenService).save(anyString(), anyString(), any(HttpServletRequest.class));

        // when
        TokenPair tokenPair = authService.handleKakaoCallback(code, request);

        // then
        assertThat(tokenPair.getAccessToken()).isEqualTo(accessToken);
        assertThat(tokenPair.getRefreshToken()).isNotNull();
        verify(kakaoOAuthService, times(1)).requestAccessToken(code);
        verify(kakaoOAuthService, times(1)).getUserInfo(kakaoAccessToken);
        verify(memberService, times(1)).findOrCreateByOAuth(oAuthUserInfo);
        verify(refreshTokenService, times(1)).save(anyString(), anyString(), any(HttpServletRequest.class));
        verify(jwtTokenProvider, times(1)).createToken("1");
    }

    @Test
    @DisplayName("로그아웃을 처리한다")
    void logout() {
        // given
        String refreshToken = "test_refresh_token";
        when(jwtTokenProvider.resolveRefreshToken(request)).thenReturn(refreshToken);
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        doNothing().when(refreshTokenService).delete(refreshToken);

        // when
        authService.logout(request, response);

        // then
        verify(jwtTokenProvider, times(1)).resolveRefreshToken(request);
        verify(jwtTokenProvider, times(1)).validateToken(refreshToken);
        verify(refreshTokenService, times(1)).delete(refreshToken);
        verify(response, times(2)).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰으로 로그아웃 요청 시 경고 로그를 남긴다")
    void logout_invalidToken() {
        // given
        String refreshToken = "invalid_refresh_token";
        when(jwtTokenProvider.resolveRefreshToken(request)).thenReturn(refreshToken);
        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(false);

        // when
        authService.logout(request, response);

        // then
        verify(jwtTokenProvider, times(1)).resolveRefreshToken(request);
        verify(jwtTokenProvider, times(1)).validateToken(refreshToken);
        verify(refreshTokenService, never()).delete(anyString());
        verify(response, times(2)).addHeader(eq("Set-Cookie"), anyString());
    }

    @Test
    @DisplayName("구글 콜백 처리 중 예외 발생 시 GlobalException을 던진다")
    void handleGoogleCallback_whenServiceFails_throwsException() {
        // given
        String code = "test_code";
        when(googleOAuthService.handleCallback(code)).thenThrow(new RuntimeException("Google API Error"));

        // when & then
        assertThrows(RuntimeException.class, () -> authService.handleGoogleCallback(code, request));

        verify(memberService, never()).findOrCreateByOAuth(any());
        verify(jwtTokenProvider, never()).createToken(anyString());
    }

    @Test
    @DisplayName("리프레시 토큰이 없을 때 로그아웃 요청 시, 토큰 삭제 없이 쿠키만 만료시킨다")
    void logout_noRefreshToken() {
        // given
        when(jwtTokenProvider.resolveRefreshToken(request)).thenReturn(null);

        // when
        authService.logout(request, response);

        // then
        verify(refreshTokenService, never()).delete(anyString());
        verify(response, times(2)).addHeader(eq("Set-Cookie"), anyString());
    }
}