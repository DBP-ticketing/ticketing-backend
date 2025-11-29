package com.DBP.ticketing_backend.domain.seat.dto;

import com.DBP.ticketing_backend.domain.seat.entity.Seat;
import com.DBP.ticketing_backend.domain.seat.enums.SeatStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeatResponseDto {
    private Long seatId;
    private String section; // 구역 (A구역)
    private Integer row; // 행
    private Integer col; // 열
    private String seatLevel; // 등급 (VIP, R)
    private Integer price; // 가격
    private SeatStatus status; // 판매 가능 여부

    public static SeatResponseDto from(Seat seat) {

        String seatLevelString = seat.getLevel() != null ? seat.getLevel().toString() : "";

        return SeatResponseDto.builder()
                .seatId(seat.getSeatId())
                .section(seat.getTemplate().getSection())
                .row(seat.getTemplate().getRow())
                .col(seat.getTemplate().getColumn())
                .seatLevel(seatLevelString)
                .price(seat.getPrice())
                .status(seat.getStatus())
                .build();
    }
}
