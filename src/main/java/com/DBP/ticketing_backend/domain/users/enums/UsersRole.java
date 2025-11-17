package com.DBP.ticketing_backend.domain.users.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UsersRole {
    ROLE_USER("ROLE_USER", "일반 사용자"),
    ROLE_ADMIN("ROLE_ADMIN", "관리자"),
    ROLE_HOST("ROLE_HOST", "호스트");

    private final String key;
    private final String description;
}
