package com.DBP.ticketing_backend.domain.booking.dto;

import lombok.Data;

@Data
public class BookingRequestDto {

    // 지정석 예매 시 필수
    private Long seatId;

    // 스탠딩/자유석 예매 시 필수 (빈자리 자동 배정용)
    private Long eventId;

}
