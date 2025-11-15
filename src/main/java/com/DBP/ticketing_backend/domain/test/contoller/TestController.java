package com.DBP.ticketing_backend.domain.test.contoller;

import com.DBP.ticketing_backend.domain.auth.dto.response.AuthResponseDto;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * JWT 인증 테스트
     */
    @GetMapping
    public ResponseEntity<AuthResponseDto<Map<String, String>>> test() {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();
        String role = authentication.getAuthorities().toString();

        Map<String, String> data = Map.of(
            "message", "JWT 인증 성공!",
            "username", username,
            "role", role
        );

        return ResponseEntity.ok(
            AuthResponseDto.success("인증된 사용자입니다.", data)
        );
    }
}
