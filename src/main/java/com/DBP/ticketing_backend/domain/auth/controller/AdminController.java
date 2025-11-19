package com.DBP.ticketing_backend.domain.auth.controller;

import com.DBP.ticketing_backend.domain.auth.dto.response.AuthResponseDto;
import com.DBP.ticketing_backend.domain.auth.dto.response.HostResponseDto;
import com.DBP.ticketing_backend.domain.auth.service.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = " 어드민 api", description = "어드민의 관리 API 입니다.")
public class AdminController {

    private final AdminService adminService;

    // 대기 중인 호스트 목록 조회
    @Operation(summary = "호스트 요청 조회", description = "호스트로 회원가입을 요청한 정보들을 조회합니다.")
    @GetMapping("host/pending")
    public ResponseEntity<AuthResponseDto<List<HostResponseDto>>> getPendingHosts() {
        try {
            List<HostResponseDto> hosts = adminService.getPendingHosts();
            return ResponseEntity.ok(AuthResponseDto.success("대기 중인 호스트 목록 조회 성공", hosts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AuthResponseDto.error(e.getMessage()));
        }
    }

    // 모든 호스트 목록 조회
    @Operation(summary = "호스트 조회", description = "호스트 정보들을 조회합니다.")
    @GetMapping("/host")
    public ResponseEntity<AuthResponseDto<List<HostResponseDto>>> getAllHosts() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Controller Auth: " + auth);
            List<HostResponseDto> hosts = adminService.getAllHosts();
            return ResponseEntity.ok(AuthResponseDto.success("호스트 목록 조회 성공", hosts));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AuthResponseDto.error(e.getMessage()));
        }
    }

    // 호스트 승인
    @Operation(summary = "호스트 승인", description = "호스트 회원가입 요청을 승인합니다.")
    @PostMapping("/{hostId}/approve")
    public ResponseEntity<AuthResponseDto<Void>> approveHost(@PathVariable Long hostId) {
        try {
            adminService.approveHost(hostId);
            return ResponseEntity.ok(AuthResponseDto.success("호스트가 승인되었습니다."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(AuthResponseDto.error(e.getMessage()));
        }
    }

    // 호스트 거부
    @Operation(summary = "호스트 거절", description = "호스트 회원가입 요청을 거절합니다.")
    @PostMapping("/{hostId}/reject")
    public ResponseEntity<AuthResponseDto<Void>> rejectHost(@PathVariable Long hostId) {
        try {
            adminService.rejectHost(hostId);
            return ResponseEntity.ok(AuthResponseDto.success("호스트가 거부되었습니다."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(AuthResponseDto.error(e.getMessage()));
        }
    }

    // 호스트 정지
    @Operation(summary = "호스트 정지", description = "호스트 회원의 계정을 정지합니다.")
    @PostMapping("/{hostId}/suspend")
    public ResponseEntity<AuthResponseDto<Void>> suspendHost(@PathVariable Long hostId) {
        try {
            adminService.suspendHost(hostId);
            return ResponseEntity.ok(AuthResponseDto.success("호스트가 정지되었습니다."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(AuthResponseDto.error(e.getMessage()));
        }
    }

    // 호스트 정지 해제
    @Operation(summary = "호스트 정지 해제", description = "호스트 회원의 계정을 정지를 해제합니다.")
    @PostMapping("/{hostId}/activate")
    public ResponseEntity<AuthResponseDto<Void>> activateHost(@PathVariable Long hostId) {
        try {
            adminService.activateHost(hostId);
            return ResponseEntity.ok(AuthResponseDto.success("호스트가 활성화되었습니다."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(AuthResponseDto.error(e.getMessage()));
        }
    }
}
