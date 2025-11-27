package com.DBP.ticketing_backend.domain.payment.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseDto {
    private Long bookingId;
    private String tid;                    // 카카오페이 결제 고유번호
    private String paymentUrl;             // 결제 페이지 URL (준비 단계)
    private String status;                 // 결제 상태
    private Integer amount;                // 결제 금액
    private String approvedAt;             // 결제 승인 시각
}
