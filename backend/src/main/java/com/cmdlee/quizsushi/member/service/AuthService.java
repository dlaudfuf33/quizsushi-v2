package com.cmdlee.quizsushi.member.service;

import com.cmdlee.quizsushi.global.auth.jwt.JwtTokenProvider;
import com.cmdlee.quizsushi.global.util.CookieUtil;
import com.cmdlee.quizsushi.member.domain.model.QuizsushiMember;
import com.cmdlee.quizsushi.member.dto.OAuthUserInfo;
import com.cmdlee.quizsushi.member.security.TokenPair;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoOAuthService kakaoOAuthService;
    private final GoogleOAuthService googleOAuthService;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    public String getGoogleLoginUrl() {
        return googleOAuthService.generateGoogleLoginUrl();
    }

    public TokenPair handleGoogleCallback(String code, HttpServletRequest request) {
        OAuthUserInfo userInfo = googleOAuthService.handleCallback(code);
        return getTokenPair(request, userInfo);
    }


    public String getKakaoLoginUrl() {
        return kakaoOAuthService.generateKakaoLoginUrl();
    }

    public TokenPair handleKakaoCallback(String code, HttpServletRequest request) {
        String kakaoAccessToken = kakaoOAuthService.requestAccessToken(code);
        OAuthUserInfo userInfo = kakaoOAuthService.getUserInfo(kakaoAccessToken);
        return getTokenPair(request, userInfo);
    }

    @NotNull
    private TokenPair getTokenPair(HttpServletRequest request, OAuthUserInfo userInfo) {
        QuizsushiMember member = memberService.findOrCreateByOAuth(userInfo);

        String refreshUUID = UUID.randomUUID().toString();
        String memberId = member.getId().toString();
        refreshTokenService.save(memberId, refreshUUID, request);

        String accessToken = jwtTokenProvider.createToken(memberId);
        return new TokenPair(accessToken, refreshUUID);
    }


    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            refreshTokenService.delete(refreshToken);
        } else {
            log.warn("Logout requested with invalid or missing refresh token: {}", refreshToken);
        }
        response.addHeader("Set-Cookie", CookieUtil.expireRefreshCookie().toString());
        response.addHeader("Set-Cookie", CookieUtil.expireAccessCookie().toString());
    }
}