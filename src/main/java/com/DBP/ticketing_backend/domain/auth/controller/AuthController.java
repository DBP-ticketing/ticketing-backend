package com.DBP.ticketing_backend.domain.auth.controller;

import com.DBP.ticketing_backend.domain.auth.dto.request.LoginRequestDto;
import com.DBP.ticketing_backend.domain.auth.dto.request.SignUpHostRequestDto;
import com.DBP.ticketing_backend.domain.auth.dto.request.SignUpUserRequestDto;
import com.DBP.ticketing_backend.domain.auth.dto.response.AuthResponseDto;
import com.DBP.ticketing_backend.domain.auth.dto.response.LoginResponseDto;
import com.DBP.ticketing_backend.domain.auth.service.AuthService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 일반 사용자 회원가입
    @PostMapping("/signup/user")
    public ResponseEntity<AuthResponseDto<Void>> signUpUser(
        @RequestBody SignUpUserRequestDto request) {
        try {
            authService.saveUser(request);
            return ResponseEntity.ok(
                AuthResponseDto.success("회원가입이 완료되었습니다.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                AuthResponseDto.error(e.getMessage())
            );
        }
    }

    // 호스트 회원가입
    @PostMapping("/signup/host")
    public ResponseEntity<AuthResponseDto<Void>> signUpHost(
        @RequestBody SignUpHostRequestDto request) {
        try {
            authService.saveHost(request);
            return ResponseEntity.ok(
                AuthResponseDto.success("회원가입이 완료되었습니다.")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                AuthResponseDto.error(e.getMessage())
            );
        }
    }

    // 로그인 (JWT 없음)
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto<LoginResponseDto>> login(
        @RequestBody LoginRequestDto request) {
        try {
            LoginResponseDto loginResponse = authService.login(
                request.getEmail(),
                request.getPassword()
            );
            return ResponseEntity.ok(
                AuthResponseDto.success("로그인 성공", loginResponse)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                AuthResponseDto.error(e.getMessage())
            );
        }
    }
}
