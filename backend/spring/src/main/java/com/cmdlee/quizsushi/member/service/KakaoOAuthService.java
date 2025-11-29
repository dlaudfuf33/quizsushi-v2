package com.cmdlee.quizsushi.member.service;

import com.cmdlee.quizsushi.member.dto.OAuthUserInfo;
import com.cmdlee.quizsushi.member.dto.kakao.KakaoUserInfo;
import com.cmdlee.quizsushi.member.dto.kakao.KakaoUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthService {
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${oauth.kakao.client-secret}")
    private String clientSecret;

    private final WebClient webClient = WebClient.builder().build();


    public String generateKakaoLoginUrl() {
        return "https://kauth.kakao.com/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + kakaoRedirectUri;
    }


    public String requestAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", kakaoRedirectUri);
        formData.add("client_secret", clientSecret);
        formData.add("code", code);

        return webClient.post()
                .uri(TOKEN_URL)
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("💥 Kakao Token Error Response: {}", errorBody);
                            return Mono.error(new RuntimeException("Kakao token error: " + errorBody));
                        })
                )
                .bodyToMono(Map.class)
                .map(body -> (String) body.get("access_token"))
                .block();
    }


    public OAuthUserInfo getUserInfo(String accessToken) {
        KakaoUserResponse response = webClient.get()
                .uri(USER_INFO_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(KakaoUserResponse.class)
                .block();

        return new KakaoUserInfo(
                response.getId(),
                response.getKakao_account().getEmail(),
                response.getKakao_account().getProfile().getNickname()
        );
    }

}
