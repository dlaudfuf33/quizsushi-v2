package com.cmdlee.quizsushi.member.service;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.global.config.infra.redis.RefreshTokenRedisService;
import com.cmdlee.quizsushi.member.domain.model.RefreshTokenData;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRedisService refreshTokenRedisService;

    public String validateAndFind(HttpServletRequest request, String refreshUUID) {
        String currentUserAgent = request.getHeader("User-Agent");
        RefreshTokenData stored = refreshTokenRedisService.find(refreshUUID)
                .orElseThrow(() -> new GlobalException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!Objects.equals(stored.getUserAgent(), currentUserAgent)) {
            throw new GlobalException(ErrorCode.TOKEN_CLIENT_MISMATCH);
        }

        return stored.getUserId();
    }

    public void save(String userId, String refreshUUID, HttpServletRequest request) {
        refreshTokenRedisService.save(userId, refreshUUID, request);
    }

    public void delete(String refreshUUID) {
        refreshTokenRedisService.delete(refreshUUID);
    }

    public void deleteByUserId(String userId) {
        refreshTokenRedisService.deleteByUserId(userId);
    }
}
