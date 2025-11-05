package com.DBP.ticketing_backend.domain.host.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HostStatus {
    ACTIVE("활성", "승인된 유저/호스트"),
    PENDING("대기", "승인 대기 중");

    private final String key;
    private final String description;
}
