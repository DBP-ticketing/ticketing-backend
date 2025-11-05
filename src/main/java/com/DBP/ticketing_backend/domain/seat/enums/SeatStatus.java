package com.DBP.ticketing_backend.domain.seat.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeatStatus {
    AVAILABLE("예매 가능", "예매 가능한 좌석"),
    BOOKED("예매됨", "예매된 좌석"),
    UNAVAILABLE("판매 불가", "판매하지 않는 좌석");

    private final String key;
    private final String description;
}
