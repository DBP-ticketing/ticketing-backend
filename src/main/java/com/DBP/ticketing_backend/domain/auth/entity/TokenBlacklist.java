package com.DBP.ticketing_backend.domain.auth.entity;

import com.DBP.ticketing_backend.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenBlacklist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blacklist_id")
    private Long blacklistId;

    @Column(nullable = false, length = 500)
    private String token;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Builder
    public TokenBlacklist(String token, String email, LocalDateTime expiryDate) {
        this.token = token;
        this.email = email;
        this.expiryDate = expiryDate;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
