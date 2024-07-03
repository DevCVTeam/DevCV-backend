package com.devcv.auth.filter;

import com.devcv.auth.dto.RefreshTokenResponse;
import com.devcv.auth.jwt.JwtProvider;
import com.devcv.auth.jwt.JwtTokenDto;
import com.devcv.common.exception.ErrorCode;
import com.devcv.common.exception.UnAuthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    // 실제 필터링 로직은 doFilterInternal 에 들어감, JWT 토큰의 인증 정보를 현재 쓰레드의 SecurityContext 에 저장하는 역할 수행
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws IOException, ServletException {
        // 1. Request Header 에서 토큰 추출.
        String accessToken = jwtProvider.resolveAccessToken(request);
        // 2. AccessToken 유/무 판별.
        try {
            log.info("REQUEST_JWTTOKEN : " + accessToken);
            if (StringUtils.hasText(accessToken)) {
                if(jwtProvider.validateToken(accessToken)){
                    // 정상 : Authentication SecurityContext에 저장. setAuthentication
                    this.setAuthentication(accessToken);
                }
            }
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            request.setAttribute("exception", ErrorCode.JWT_INVALID_SIGN_ERROR.getMessage());
        } catch (ExpiredJwtException e){
            request.setAttribute("exception", ErrorCode.JWT_EXPIRED_ERROR.getMessage());
        } catch (UnsupportedJwtException e) {
            request.setAttribute("exception", ErrorCode.JWT_UNSUPPORTED_ERROR.getMessage());
        } catch (IllegalArgumentException e) {
            request.setAttribute("exception", ErrorCode.JWT_ILLEGALARGUMENT_ERROR.getMessage());
        } catch (Exception e) {
            request.setAttribute("exception", ErrorCode.JWTILLEGALARG_ERROR.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String accessToken) {
        Authentication authentication = jwtProvider.getAuthentication(accessToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
