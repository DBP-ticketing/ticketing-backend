package com.DBP.ticketing_backend.domain.booking.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BookingStatus {
    PENDING("결제 대기", "예약 생성 후 결제 대기 중"),
    CONFIRMED("예약 완료", "결제 완료 및 예약 확정"),
    CANCELLED("취소됨", "예약 취소됨");

    private final String key;
    private final String description;
}
