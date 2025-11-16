package com.DBP.ticketing_backend.domain.auth.controller;

import com.DBP.ticketing_backend.domain.auth.dto.request.LoginRequestDto;
import com.DBP.ticketing_backend.domain.auth.dto.request.SignUpHostRequestDto;
import com.DBP.ticketing_backend.domain.auth.dto.request.SignUpUserRequestDto;
import com.DBP.ticketing_backend.domain.auth.dto.response.AuthResponseDto;
import com.DBP.ticketing_backend.domain.auth.dto.response.LoginResponseDto;
import com.DBP.ticketing_backend.domain.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 일반 사용자 회원가입 */
    @PostMapping("/signup/user")
    public ResponseEntity<AuthResponseDto<Void>> signUpUser(
            @RequestBody SignUpUserRequestDto request) {
        try {
            authService.saveUser(request);
            return ResponseEntity.ok(AuthResponseDto.success("회원가입이 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(AuthResponseDto.error(e.getMessage()));
        }
    }

    /** 호스트 회원가입 */
    @PostMapping("/signup/host")
    public ResponseEntity<AuthResponseDto<Void>> signUpHost(
            @RequestBody SignUpHostRequestDto request) {
        try {
            authService.saveHost(request);
            return ResponseEntity.ok(AuthResponseDto.success("회원가입이 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(AuthResponseDto.error(e.getMessage()));
        }
    }

    /** 로그인 */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto<LoginResponseDto>> login(
            @RequestBody LoginRequestDto request) {
        try {
            LoginResponseDto loginResponse =
                    authService.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(AuthResponseDto.success("로그인 성공", loginResponse));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(AuthResponseDto.error(e.getMessage()));
        }
    }

    /** Access Token 재발급 */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto<Map<String, String>>> refresh(
            @RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");

            if (refreshToken == null) {
                return ResponseEntity.badRequest()
                        .body(AuthResponseDto.error("Refresh Token이 필요합니다."));
            }

            String newAccessToken = authService.refreshAccessToken(refreshToken);

            return ResponseEntity.ok(
                    AuthResponseDto.success(
                            "Access Token이 재발급되었습니다.", Map.of("accessToken", newAccessToken)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(AuthResponseDto.error(e.getMessage()));
        }
    }

    /** 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponseDto<Void>> logout(
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Bearer 토큰 추출
            String token = null;
            if (StringUtils.hasText(authorizationHeader)
                    && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
            }

            if (token == null) {
                return ResponseEntity.badRequest().body(AuthResponseDto.error("토큰이 필요합니다."));
            }

            authService.logout(token);
            return ResponseEntity.ok(AuthResponseDto.success("로그아웃되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AuthResponseDto.error(e.getMessage()));
        }
    }
}
