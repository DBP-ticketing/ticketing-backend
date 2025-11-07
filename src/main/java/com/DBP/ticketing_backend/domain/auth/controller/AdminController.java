package com.DBP.ticketing_backend.domain.auth.controller;

import com.DBP.ticketing_backend.domain.auth.dto.response.AuthResponseDto;
import com.DBP.ticketing_backend.domain.auth.dto.response.HostResponseDto;
import com.DBP.ticketing_backend.domain.auth.service.AdminService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/hosts")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // 대기 중인 호스트 목록 조회
    @GetMapping("/pending")
    public ResponseEntity<AuthResponseDto<List<HostResponseDto>>> getPendingHosts() {
        try {
            List<HostResponseDto> hosts = adminService.getPendingHosts();
            return ResponseEntity.ok(
                AuthResponseDto.success("대기 중인 호스트 목록 조회 성공", hosts)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                AuthResponseDto.error(e.getMessage())
            );
        }
    }

    // 모든 호스트 목록 조회
    @GetMapping
    public ResponseEntity<AuthResponseDto<List<HostResponseDto>>> getAllHosts() {
        try {
            List<HostResponseDto> hosts = adminService.getAllHosts();
            return ResponseEntity.ok(
                AuthResponseDto.success("호스트 목록 조회 성공", hosts)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                AuthResponseDto.error(e.getMessage())
            );
        }
    }

    // 호스트 승인
    @PostMapping("/{hostId}/approve")
    public ResponseEntity<AuthResponseDto<Void>> approveHost(@PathVariable Long hostId) {
        try {
            adminService.approveHost(hostId);
            return ResponseEntity.ok(
                AuthResponseDto.success("호스트가 승인되었습니다.")
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(
                AuthResponseDto.error(e.getMessage())
            );
        }
    }

    // 호스트 거부
    @PostMapping("/{hostId}/reject")
    public ResponseEntity<AuthResponseDto<Void>> rejectHost(@PathVariable Long hostId) {
        try {
            adminService.rejectHost(hostId);
            return ResponseEntity.ok(
                AuthResponseDto.success("호스트가 거부되었습니다.")
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(
                AuthResponseDto.error(e.getMessage())
            );
        }
    }

    // 호스트 정지
    @PostMapping("/{hostId}/suspend")
    public ResponseEntity<AuthResponseDto<Void>> suspendHost(@PathVariable Long hostId) {
        try {
            adminService.suspendHost(hostId);
            return ResponseEntity.ok(
                AuthResponseDto.success("호스트가 정지되었습니다.")
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(
                AuthResponseDto.error(e.getMessage())
            );
        }
    }

    // 호스트 정지 해제
    @PostMapping("/{hostId}/activate")
    public ResponseEntity<AuthResponseDto<Void>> activateHost(@PathVariable Long hostId) {
        try {
            adminService.activateHost(hostId);
            return ResponseEntity.ok(
                AuthResponseDto.success("호스트가 활성화되었습니다.")
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(
                AuthResponseDto.error(e.getMessage())
            );
        }
    }

}
