package com.DBP.ticketing_backend.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자"),
    HOST("ROLE_HOST", "호스트");

    private final String key;
    private final String description;
}
