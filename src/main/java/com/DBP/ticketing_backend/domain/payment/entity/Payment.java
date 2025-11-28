package com.DBP.ticketing_backend.domain.payment.entity;

import com.DBP.ticketing_backend.domain.booking.entity.Booking;
import com.DBP.ticketing_backend.domain.payment.enums.PaymentStatus;
import com.DBP.ticketing_backend.global.common.BaseEntity;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(nullable = false, unique = true)
    private String tid; // 카카오페이 결제 고유번호

    @Column(nullable = false)
    private Integer amount; // 결제 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status; // 결제 상태

    @Column(length = 50)
    private String paymentMethod; // 결제 수단 (CARD, MONEY 등)

    private LocalDateTime approvedAt; // 결제 승인 시각

    @Builder
    public Payment(Booking booking, String tid, Integer amount, PaymentStatus status) {
        this.booking = booking;
        this.tid = tid;
        this.amount = amount;
        this.status = status != null ? status : PaymentStatus.READY;
    }

    public void approve(String paymentMethod, LocalDateTime approvedAt) {
        this.status = PaymentStatus.APPROVED;
        this.paymentMethod = paymentMethod;
        this.approvedAt = approvedAt;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }
}
