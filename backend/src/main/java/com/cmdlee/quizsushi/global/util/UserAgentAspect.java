package com.cmdlee.quizsushi.global.util;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class UserAgentAspect {

    private final HttpServletRequest request;

    @Before("@annotation(com.cmdlee.quizsushi.global.util.RejectBot) || @within(com.cmdlee.quizsushi.global.util.RejectBot)")
    public void checkUserAgent() {
        String ua = Optional.ofNullable(request.getHeader("User-Agent")).orElse("").toLowerCase();
        List<String> blocked = List.of("curl", "python", "postman", "node", "bot");

        for (String agent : blocked) {
            if (ua.contains(agent)) {
                throw new GlobalException(ErrorCode.BOT_ACCESS_BLOCKED);
            }
        }
    }
}