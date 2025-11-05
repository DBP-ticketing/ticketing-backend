package com.DBP.ticketing_backend.domain.event.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventCategory {

    CONCERT("콘서트"),
    MUSICAL("뮤지컬"),
    THEATER("연극"),
    SPORTS("스포츠"),
    EXHIBITION("전시회"),
    ETC("기타");

    private final String key;
}
