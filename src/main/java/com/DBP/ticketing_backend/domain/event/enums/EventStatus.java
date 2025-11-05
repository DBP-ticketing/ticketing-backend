package com.DBP.ticketing_backend.domain.event.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventStatus {

    SCHEDULED("예매 전", "예매 시작 전"),
    OPEN("예매 중", "예매 가능"),
    CLOSED("예매 마감", "예매 종료"),
    CANCELLED("취소됨", "이벤트 취소"),
    COMPLETED("종료", "이벤트 완료");

    private final String key;
    private final String description;
}
