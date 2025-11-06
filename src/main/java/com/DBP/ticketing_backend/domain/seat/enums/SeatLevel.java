package com.DBP.ticketing_backend.domain.seat.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeatLevel {
    VIP("VIP석"),
    R("R석"),
    S("S석"),
    A("A석"),
    B("B석"),
    C("C석");

    private final String key;
}
