package com.DBP.ticketing_backend.global.config;

import com.DBP.ticketing_backend.domain.auth.exception.JwtAccessDeniedHandler;
import com.DBP.ticketing_backend.domain.auth.exception.JwtAuthenticationEntryPoint;
import com.DBP.ticketing_backend.domain.auth.service.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // 예외 처리
                .exceptionHandling(
                        exception -> {
                            exception.authenticationEntryPoint(jwtAuthenticationEntryPoint); // 401
                            exception.accessDeniedHandler(jwtAccessDeniedHandler); // 403
                        })
                // 경로별 인증 설정
                .authorizeHttpRequests(
                        auth -> {
                            // 인증 없이 접근 가능한 경로
                            auth.requestMatchers(
                                            "/api/auth/signup/**", // 회원가입
                                            "/api/auth/login", // 로그인
                                            "/api/auth/refresh", // 토큰 재발급
                                            "/api/events",
                                            "/api/events/{eventId}"
                                            )
                                    .permitAll();

                            // 로그아웃은 인증 필요
                            auth.requestMatchers("/api/auth/logout").authenticated();

                            // ADMIN만 접근 가능
                            auth.requestMatchers("/api/admin/**").hasRole("ADMIN");
                            auth.requestMatchers("/api/place/**").hasRole("ADMIN");

                            // HOST만 접근 가능
                            auth.requestMatchers("/api/host/**").hasRole("HOST");

                            // USER, HOST 모두 접근 가능
                            auth.requestMatchers("/api/**").authenticated();

                            // 나머지는 인증 필요
                            auth.anyRequest().authenticated();
                        })

                // JWT 필터 추가
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
