package com.DBP.ticketing_backend.domain.auth.controller;

import com.DBP.ticketing_backend.domain.auth.dto.request.SignUpHostRequestDto;
import com.DBP.ticketing_backend.domain.auth.dto.request.SignUpUserRequestDto;
import com.DBP.ticketing_backend.domain.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 일반 사용자 회원가입
    @PostMapping("/signup/user")
    public ResponseEntity<?> signUpUser(@RequestBody SignUpUserRequestDto request) {
        try {
            authService.saveUser(request);
            return ResponseEntity.ok(Map.of("success", true, "message", "회원가입이 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 호스트 회원가입
    @PostMapping("/signup/host")
    public ResponseEntity<?> signUpHost(@RequestBody SignUpHostRequestDto request) {
        try {
            authService.saveHost(request);
            return ResponseEntity.ok(
                    Map.of("success", true, "message", "호스트 회원가입이 완료되었습니다. 승인을 기다려주세요."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 로그인 (JWT 없음)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        try {
            boolean isValid = authService.validateUser(email, password);

            if (isValid) {
                return ResponseEntity.ok(
                        Map.of("success", true, "message", "로그인 성공", "email", email));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "이메일 또는 비밀번호가 올바르지 않습니다."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
