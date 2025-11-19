package com.DBP.ticketing_backend.domain.auth.controller;

import com.DBP.ticketing_backend.domain.auth.dto.request.LoginRequestDto;
import com.DBP.ticketing_backend.domain.auth.dto.request.SignUpHostRequestDto;
import com.DBP.ticketing_backend.domain.auth.dto.request.SignUpUserRequestDto;
import com.DBP.ticketing_backend.domain.auth.dto.response.AuthResponseDto;
import com.DBP.ticketing_backend.domain.auth.dto.response.LoginResponseDto;
import com.DBP.ticketing_backend.domain.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

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
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = " Auth api", description = "인증/인가와 관련된 API 입니다.")
public class AuthController {

    private final AuthService authService;

    /** 일반 사용자 회원가입 */
    @Operation(summary = "유저 회원가입", description = "일반 유저로 회원가입 요청을 합니다.")
    @PostMapping("/signup/user")
    public ResponseEntity<AuthResponseDto<Void>> signUpUser(
            @Valid @RequestBody SignUpUserRequestDto request) {
        try {
            authService.saveUser(request);
            return ResponseEntity.ok(AuthResponseDto.success("회원가입이 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(AuthResponseDto.error(e.getMessage()));
        }
    }

    /** 호스트 회원가입 */
    @Operation(summary = "호스트 회원가입", description = "호스트로 회원가입 요청을 합니다.")
    @PostMapping("/signup/host")
    public ResponseEntity<AuthResponseDto<Void>> signUpHost(
            @Valid @RequestBody SignUpHostRequestDto request) {
        try {
            authService.saveHost(request);
            return ResponseEntity.ok(AuthResponseDto.success("회원가입 요청이 완료되었습니다. 승인을 기다려주세요."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(AuthResponseDto.error(e.getMessage()));
        }
    }

    /** 로그인 */
    @Operation(summary = "로그인", description = "로그인을 합니다.")
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
    @Operation(summary = "로그아웃", description = "로그아웃을 합니다.")
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
