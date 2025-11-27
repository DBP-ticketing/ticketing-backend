package com.DBP.ticketing_backend.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    READY("결제 준비", "결제 준비 단계"),
    APPROVED("결제 승인", "결제 완료"),
    CANCELLED("결제 취소", "사용자가 결제 취소"),
    FAILED("결제 실패", "결제 실패");

    private final String key;
    private final String description;
}