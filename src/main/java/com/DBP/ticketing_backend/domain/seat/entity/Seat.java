package com.DBP.ticketing_backend.domain.seat.entity;

import com.DBP.ticketing_backend.domain.event.entity.Event;
import com.DBP.ticketing_backend.domain.seat.enums.SeatLevel;
import com.DBP.ticketing_backend.domain.seat.enums.SeatStatus;
import com.DBP.ticketing_backend.domain.seattemplate.entity.SeatTemplate;
import com.DBP.ticketing_backend.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Long seatId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private SeatTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SeatLevel level;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatStatus status;

    @Builder
    public Seat(
            Event event, SeatTemplate template, SeatLevel level, Integer price, SeatStatus status) {
        this.event = event;
        this.template = template;
        this.level = level;
        this.price = price;
        this.status = status != null ? status : SeatStatus.AVAILABLE;
    }

    public void updateStatus(SeatStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("좌석 상태는 null일 수 없습니다.");
        }
        this.status = status;
    }
}
