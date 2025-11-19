package com.DBP.ticketing_backend.domain.event.dto;

import com.DBP.ticketing_backend.domain.event.entity.Event;
import com.DBP.ticketing_backend.domain.event.enums.EventCategory;
import com.DBP.ticketing_backend.domain.event.enums.EventStatus;
import com.DBP.ticketing_backend.domain.event.enums.SeatForm;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDetailResponseDto {

    private Long eventId;
    private String eventName;
    private String hostName;
    private String placeName;
    private String address;       // 상세에는 주소 필요
    private LocalDateTime date;
    private LocalDateTime ticketingStartAt; // 예매 오픈 시간
    private EventCategory category;
    private SeatForm seatForm;    // 좌석 형태 (지정석/스탠딩)
    private EventStatus status;

    public static EventDetailResponseDto from(Event event) {
        return EventDetailResponseDto.builder()
            .eventId(event.getEventId())
            .eventName(event.getEventName())
            .hostName(event.getHost().getCompanyName())
            .placeName(event.getPlace().getPlaceName())
            .address(event.getPlace().getAddress()) // 주소 추가
            .date(event.getDate())
            .ticketingStartAt(event.getTicketingStartAt())
            .category(event.getCategory())
            .seatForm(event.getSeatForm())
            .status(event.getStatus())
            .build();
    }
}