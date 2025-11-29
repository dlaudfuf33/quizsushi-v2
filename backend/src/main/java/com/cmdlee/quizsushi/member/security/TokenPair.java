package com.cmdlee.quizsushi.member.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenPair {
    private final String accessToken;
    private final String refreshToken;
}
