package com.DBP.ticketing_backend.domain.event.dto;

import com.DBP.ticketing_backend.domain.event.entity.Event;
import com.DBP.ticketing_backend.domain.event.enums.EventCategory;
import com.DBP.ticketing_backend.domain.event.enums.EventStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventListResponseDto {

    private Long eventId;
    private String eventName;
    private String hostName; // 호스트명 (회사명)
    private String placeName; // 장소명
    private LocalDateTime date; // 공연 날짜
    private EventCategory category;
    private EventStatus status; // 예매중/마감 등 상태

    public static EventListResponseDto from(Event event) {
        return EventListResponseDto.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .hostName(event.getHost().getCompanyName()) // Host 엔티티 접근
                .placeName(event.getPlace().getPlaceName()) // Place 엔티티 접근
                .date(event.getDate())
                .category(event.getCategory())
                .status(event.getStatus())
                .build();
    }
}
