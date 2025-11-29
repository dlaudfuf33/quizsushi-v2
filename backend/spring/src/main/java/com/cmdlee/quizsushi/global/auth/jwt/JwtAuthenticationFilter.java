package com.cmdlee.quizsushi.global.auth.jwt;

import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.global.util.CookieUtil;
import com.cmdlee.quizsushi.global.config.security.member.CustomMemberDetails;
import com.cmdlee.quizsushi.member.service.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        String accessToken = jwtTokenProvider.resolveAccessToken(request);
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            setAuthentication(accessToken, request);
        } else {
            if (accessToken == null) log.debug("No access token in request.");
            tryAutoRefresh(request, response);
        }
        filterChain.doFilter(request, response);
    }

    private void tryAutoRefresh(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
            if (refreshToken == null) {
                log.debug("No refresh token in request.");
                return;
            }
            String memberId = refreshTokenService.validateAndFind(request, refreshToken);

            String newAccessToken = jwtTokenProvider.createToken(memberId);
            ResponseCookie qtka = CookieUtil.createAccessCookie(newAccessToken);
            response.addHeader("Set-Cookie", qtka.toString());

            setAuthentication(newAccessToken, request);
            log.debug("New access token created from refresh token.");
        } catch (GlobalException e) {
            log.warn("Cannot create new access token: {}", e.getMessage());
        }
    }

    private void setAuthentication(String token, HttpServletRequest request) {
        Long userId = jwtTokenProvider.getUserId(token);

        CustomMemberDetails userDetails = new CustomMemberDetails(userId);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

}
