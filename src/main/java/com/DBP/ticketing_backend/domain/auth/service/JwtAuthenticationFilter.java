package com.DBP.ticketing_backend.domain.auth.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException {

        try {
            // 1. Request Header에서 JWT 토큰 추출
            String token = getJwtFromRequest(request);
            String requestURI = request.getRequestURI(); // 요청 주소 확인용

            // 2. 토큰 검증
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

                // 3. 블랙리스트 체크
                if (authService.isTokenBlacklisted(token)) {
                    log.warn("블랙리스트에 등록된 토큰입니다.");
                    // 인증 처리하지 않고 다음 필터로 넘김 (인증 실패 상태)
                } else {
                    // 4. 토큰에서 이메일 추출
                    String email = jwtTokenProvider.getEmailFromToken(token);

                    // 5. 이메일로 사용자 정보 조회
                    UserDetails userDetails = authService.loadUserByUsername(email);
                    log.info("=================================================");
                    log.info("🚦 Security Filter Check");
                    log.info("1. Request URI      : {}", requestURI);
                    log.info("2. Authenticated User : {}", email);
                    // 이 부분이 [ROLE_ADMIN] 인지 [ADMIN] 인지 확인해야 합니다.
                    log.info("3. Loaded Authorities : {}", userDetails.getAuthorities());
                    log.info("=================================================");

                    // 6. Authentication 객체 생성
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // 7. SecurityContext에 Authentication 저장
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("인증 성공: {}", email);
                }
            }
        } catch (Exception e) {
            log.error("인증 실패", e);
        }

        // 7. 다음 필터로 진행
        try {
            filterChain.doFilter(request, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Request Header에서 JWT 토큰 추출 */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Bearer 토큰인지 확인
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
