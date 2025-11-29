package com.DBP.ticketing_backend.domain.event.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeatForm {
    ASSIGNED("지정좌석", "좌석 번호가 지정된 형태"),
    FREE("자유좌석", "구역만 지정되고 좌석은 자유"),
    STANDING("스탠딩", "좌석 없이 서서 관람"),
    SEAT_WITH_SECTION("구역별 지정좌석", "구역에 따라 등급 및 가격이 지정된 형태");

    private final String key;
    private final String description;
}
