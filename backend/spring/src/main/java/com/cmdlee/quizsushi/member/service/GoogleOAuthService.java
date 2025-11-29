package com.cmdlee.quizsushi.member.service;

import com.cmdlee.quizsushi.member.dto.google.GoogleUserInfo;
import com.cmdlee.quizsushi.member.dto.OAuthUserInfo;
import com.cmdlee.quizsushi.member.dto.google.GoogleUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    @Value("${oauth.google.client-id}")
    private String clientId;

    @Value("${oauth.google.client-secret}")
    private String clientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String redirectUri;

    private final WebClient webClient = WebClient.builder().build();

    public String generateGoogleLoginUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("profile email", StandardCharsets.UTF_8)
                + "&state=" + UUID.randomUUID();
    }

    public OAuthUserInfo handleCallback(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);
        formData.add("grant_type", "authorization_code");

        Map<String, Object> tokenResponse = webClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();

        String accessToken = (String) tokenResponse.get("access_token");
        GoogleUserResponse userInfo = webClient.get()
                .uri("https://www.googleapis.com/oauth2/v1/userinfo?alt=json")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GoogleUserResponse.class)
                .block();

        return new GoogleUserInfo(userInfo.getId(), userInfo.getEmail(), userInfo.getName());
    }
}