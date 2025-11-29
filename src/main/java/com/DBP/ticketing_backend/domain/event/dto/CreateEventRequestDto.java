package com.DBP.ticketing_backend.domain.event.dto;

import com.DBP.ticketing_backend.domain.event.enums.EventCategory;
import com.DBP.ticketing_backend.domain.event.enums.SeatForm;
import com.DBP.ticketing_backend.domain.seat.enums.SeatLevel;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateEventRequestDto {

    @NotNull(message = "장소 ID는 필수입니다.")
    private Long placeId;

    @NotBlank(message = "이벤트명은 필수입니다.")
    private String eventName;

    @NotNull(message = "카테고리는 필수입니다.")
    private EventCategory category;

    @NotNull(message = "이벤트 날짜는 필수입니다.")
    @Future(message = "이벤트 날짜는 현재 시간보다 미래여야 합니다.")
    private LocalDateTime date;

    @NotNull(message = "예매 오픈 일시는 필수입니다.")
    private LocalDateTime ticketingStartAt;

    @NotNull(message = "좌석 형태는 필수입니다.")
    private SeatForm seatForm;

    // 핵심: 구역별 가격/등급 설정 리스트
    @NotEmpty(message = "좌석 가격 설정은 필수입니다.")
    private List<SectionSetting> seatSettings;

    @Data
    public static class SectionSetting {
        @NotNull(message = "구역명은 필수입니다.")
        private String sectionName; // 예: "A", "1층 R석" (Template의 section과 일치해야 함)

        @NotNull(message = "좌석 등급은 필수입니다.")
        private SeatLevel seatLevel; // 예: VIP, R, S

        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        private Integer price;
    }
}
