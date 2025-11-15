package com.DBP.ticketing_backend.domain.auth.repository;

import com.DBP.ticketing_backend.domain.auth.entity.TokenBlacklist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    // 토큰이 블랙리스트에 있는지 확인
    boolean existsByToken(String token);

    // 만료된 토큰 삭제 (스케줄러로 주기적 실행)
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}
