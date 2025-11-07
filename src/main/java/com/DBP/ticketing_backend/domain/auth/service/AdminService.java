package com.DBP.ticketing_backend.domain.auth.service;

import com.DBP.ticketing_backend.domain.auth.dto.response.HostResponseDto;
import com.DBP.ticketing_backend.domain.host.entity.Host;
import com.DBP.ticketing_backend.domain.host.enums.HostStatus;
import com.DBP.ticketing_backend.domain.host.repository.HostRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final HostRepository hostRepository;

    // 대기 중인 호스트 목록 조회
    public List<HostResponseDto> getPendingHosts() {
        List<Host> hosts = hostRepository.findByStatus(HostStatus.PENDING);
        return hosts.stream().map(HostResponseDto::from).collect(Collectors.toList());
    }

    // 모든 호스트 목록 조회
    public List<HostResponseDto> getAllHosts() {
        List<Host> hosts = hostRepository.findAll();
        return hosts.stream().map(HostResponseDto::from).collect(Collectors.toList());
    }

    // 호스트 승인
    public void approveHost(Long hostId) {
        Host host =
                hostRepository
                        .findById(hostId)
                        .orElseThrow(() -> new IllegalArgumentException("호스트를 찾을 수 없습니다."));

        if (host.getStatus() != HostStatus.PENDING) {
            throw new IllegalStateException("대기 중인 호스트만 승인할 수 있습니다.");
        }

        host.approve();
    }

    // 호스트 거부
    public void rejectHost(Long hostId) {
        Host host =
                hostRepository
                        .findById(hostId)
                        .orElseThrow(() -> new IllegalArgumentException("호스트를 찾을 수 없습니다."));

        if (host.getStatus() != HostStatus.PENDING) {
            throw new IllegalStateException("대기 중인 호스트만 거부할 수 있습니다.");
        }

        // 호스트 거부
        host.reject();
    }

    // 호스트 정지
    public void suspendHost(Long hostId) {
        Host host =
                hostRepository
                        .findById(hostId)
                        .orElseThrow(() -> new IllegalArgumentException("호스트를 찾을 수 없습니다."));

        if (host.getStatus() != HostStatus.ACTIVE) {
            throw new IllegalStateException("활성화된 호스트만 정지할 수 있습니다.");
        }

        host.suspend();
    }

    // 호스트 정지 해제
    public void activateHost(Long hostId) {
        Host host =
                hostRepository
                        .findById(hostId)
                        .orElseThrow(() -> new IllegalArgumentException("호스트를 찾을 수 없습니다."));

        if (host.getStatus() != HostStatus.SUSPENDED) {
            throw new IllegalStateException("정지된 호스트만 활성화할 수 있습니다.");
        }

        host.approve();
    }
}
