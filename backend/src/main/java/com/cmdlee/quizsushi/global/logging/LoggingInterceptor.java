package com.cmdlee.quizsushi.global.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug(String.format("""
                        ▶ Request URL  : %s
                        ▶ HTTP Method  : %s
                        ▶ 요청 IP       : %s
                        ▶ 사용자        : %s
                        ▶ 권한 목록     : %s
                        """,
                request.getRequestURI(),
                request.getMethod(),
                request.getRemoteAddr(),
                (auth != null ? auth.getName() : "비로그인"),
                (auth != null ? auth.getAuthorities() : "없음")
        ));

        return true;
    }
}