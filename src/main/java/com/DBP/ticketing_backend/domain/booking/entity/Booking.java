package com.DBP.ticketing_backend.domain.booking.entity;

import com.DBP.ticketing_backend.domain.booking.enums.BookingStatus;
import com.DBP.ticketing_backend.domain.users.entity.Users;
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
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(nullable = false)
    private Integer totalPrice;

    @Builder
    public Booking(Users users, BookingStatus status, Integer totalPrice) {
        this.users = users;
        this.status = status != null ? status : BookingStatus.PENDING;
        this.totalPrice = totalPrice;
    }

    public void updateStatus(BookingStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("예약 상태는 null일 수 없습니다.");
        }
        this.status = status;
    }
}
