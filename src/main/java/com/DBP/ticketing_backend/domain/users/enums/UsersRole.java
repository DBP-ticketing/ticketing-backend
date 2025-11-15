package com.DBP.ticketing_backend.domain.users.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UsersRole {
    USER("USER", "일반 사용자"),
    ADMIN("ADMIN", "관리자"),
    HOST("HOST", "호스트");

    private final String key;
    private final String description;
}
