package com.cmdlee.quizsushi.member.controller;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.global.util.CookieUtil;
import com.cmdlee.quizsushi.global.util.RejectBot;
import com.cmdlee.quizsushi.member.security.TokenPair;
import com.cmdlee.quizsushi.member.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RejectBot
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private static final String KAKAO = "kakao";
    private static final String GOOGLE = "google";

    @Value("${app.frontend.oauth-redirect}")
    private String frontendRedirectUri;

    private final AuthService authService;


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                       HttpServletResponse response
    ) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/google/login")
    public void redirectToGoogle(HttpServletResponse response
    ) {
        try {
            String googleLoginUrl = authService.getGoogleLoginUrl();
            response.sendRedirect(googleLoginUrl);
        } catch (IOException e) {
            throw new GlobalException(ErrorCode.OAUTH_REDIRECT_FAILED);
        }
    }

    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam("code") String code,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        handleOauthCallback(code, request, response, GOOGLE);
    }

    @GetMapping("/kakao/login")
    public void redirectToKakaoLogin(HttpServletResponse response
    ) {
        try {
            String kakaoLoginUrl = authService.getKakaoLoginUrl();
            response.sendRedirect(kakaoLoginUrl);
        } catch (IOException e) {
            throw new GlobalException(ErrorCode.OAUTH_REDIRECT_FAILED);
        }
    }


    @GetMapping("/kakao/callback")
    public void kakaoCallback(@RequestParam("code") String code,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        handleOauthCallback(code, request, response, KAKAO);
    }

    private void handleOauthCallback(String code, HttpServletRequest request, HttpServletResponse response, String provider) {
        try {
            TokenPair tokens = switch (provider) {
                case GOOGLE -> authService.handleGoogleCallback(code, request);
                case KAKAO -> authService.handleKakaoCallback(code, request);
                default -> throw new GlobalException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
            };

            setTokenCookies(response, tokens);
            response.sendRedirect(frontendRedirectUri);

        } catch (GlobalException e) {
            if (e.getErrorCode() == ErrorCode.BANNED_MEMBER) throw e;
            throw getCallbackError(provider);
        } catch (Exception e) {
            throw getCallbackError(provider);
        }
    }

    private void setTokenCookies(HttpServletResponse response, TokenPair tokens) {
        response.addHeader("Set-Cookie", CookieUtil.createAccessCookie(tokens.getAccessToken()).toString());
        response.addHeader("Set-Cookie", CookieUtil.createRefreshCookie(tokens.getRefreshToken()).toString());
    }

    private GlobalException getCallbackError(String provider) {
        return switch (provider) {
            case GOOGLE -> new GlobalException(ErrorCode.OAUTH_GOOGLE_CALLBACK_FAILED);
            case KAKAO -> new GlobalException(ErrorCode.OAUTH_KAKAO_CALLBACK_FAILED);
            default -> new GlobalException(ErrorCode.INTERNAL_SERVER_ERROR);
        };
    }
}