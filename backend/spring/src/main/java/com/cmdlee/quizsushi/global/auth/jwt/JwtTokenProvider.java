package com.cmdlee.quizsushi.global.auth.jwt;

import com.cmdlee.quizsushi.global.util.CookieUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key key;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 엑세스 토큰 생성
    public String createToken(String id) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(String.valueOf(id))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }


    public Long getUserId(String token) {
        return Long.parseLong(Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject());
    }


    // 엑세스 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT expired: {}", e.getMessage());
        } catch (JwtException e) {
            log.warn("JWT invalid: {}", e.getMessage());
        }
        return false;
    }

    // 요청 헤더에서 엑세스 JWT 추출
    public String resolveAccessToken(HttpServletRequest request) {
        return CookieUtil.extract(request, "qtka").orElse(null);
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        return CookieUtil.extract(request, "qtkr").orElse(null);
    }


}
