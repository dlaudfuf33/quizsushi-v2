package com.cmdlee.quizsushi.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

public class CookieUtil {
    private CookieUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static final String ACCESS_COOKIE_NAME = "qtka";
    private static final String REFRESH_COOKIE_NAME = "qtkr";

    private static ResponseCookie.ResponseCookieBuilder baseBuilder(String name, String value) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict");
    }

    public static ResponseCookie createAccessCookie(String token) {
        return baseBuilder(ACCESS_COOKIE_NAME, token)
                .maxAge(Duration.ofMinutes(10))
                .build();
    }

    public static ResponseCookie createRefreshCookie(String token) {
        return baseBuilder(REFRESH_COOKIE_NAME, token)
                .maxAge(Duration.ofDays(1))
                .build();
    }

    public static ResponseCookie expireAccessCookie() {
        return baseBuilder(ACCESS_COOKIE_NAME, "")
                .maxAge(0)
                .build();
    }

    public static ResponseCookie expireRefreshCookie() {
        return baseBuilder(REFRESH_COOKIE_NAME, "")
                .maxAge(0)
                .build();
    }

    public static Optional<String> extract(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst();
    }
}