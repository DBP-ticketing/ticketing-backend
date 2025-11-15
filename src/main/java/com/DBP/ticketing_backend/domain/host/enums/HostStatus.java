package com.DBP.ticketing_backend.domain.host.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HostStatus {
    ACTIVE("활성", "승인된 유저/호스트"),
    PENDING("대기", "승인 대기 중"),
    REJECTED("거부", "승인 거부됨"),
    SUSPENDED("정지", "활동 정지된 호스트");

    private final String key;
    private final String description;
}
