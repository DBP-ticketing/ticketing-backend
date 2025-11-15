package com.DBP.ticketing_backend.domain.auth.repository;

import com.DBP.ticketing_backend.domain.auth.entity.RefreshToken;
import com.DBP.ticketing_backend.domain.users.entity.Users;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(Users user);

    void deleteByUser(Users user);

    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}























