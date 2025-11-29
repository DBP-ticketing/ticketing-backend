package com.DBP.ticketing_backend.domain.booking.dto;

import com.DBP.ticketing_backend.domain.booking.entity.Booking;
import com.DBP.ticketing_backend.domain.booking.enums.BookingStatus;
import com.DBP.ticketing_backend.domain.event.enums.SeatForm;
import com.DBP.ticketing_backend.domain.seat.entity.Seat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingResponseDto {

    private Long bookingId;
    private Long eventId; // 추가
    private String eventName; // 이벤트명

    // 좌석 정보
    private String section; // 구역 (예: A구역)
    private Integer seatRow; // 행
    private Integer seatCol; // 열

    private SeatForm seatForm; // <--- 추가된 부분

    private Integer price; // 가격
    private BookingStatus status; // 상태 (PENDING)

    public static BookingResponseDto from(Booking booking, Seat seat) {

        SeatForm seatForm = seat.getEvent().getSeatForm();
        // 지정좌석 형태는 모두 행/열 정보를 포함합니다.
        boolean isAssigned = (seatForm == SeatForm.ASSIGNED || seatForm == SeatForm.SEAT_WITH_SECTION);

        // 구역(section)도 지정좌석일 경우에만 포함하고, 아니면 null 처리 <--- 수정된 부분
        String sectionValue = isAssigned ? seat.getTemplate().getSection() : null;

        return BookingResponseDto.builder()
            .bookingId(booking.getBookingId())
            .eventId(seat.getEvent().getEventId()) // 추가
            // Seat를 통해 Event와 Template 정보에 접근
            .eventName(seat.getEvent().getEventName())
            .section(sectionValue) // <--- 수정된 부분
            // 지정석인 경우에만 행과 열 정보를 포함
            .seatRow(isAssigned ? seat.getTemplate().getRow() : null)
            .seatCol(isAssigned ? seat.getTemplate().getColumn() : null)
            .seatForm(seatForm)
            .price(booking.getTotalPrice())
            .status(booking.getStatus())
            .build();
    }
}