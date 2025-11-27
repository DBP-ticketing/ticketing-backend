package com.DBP.ticketing_backend.domain.payment.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KakaoPayApproveRequestDto {
    private String cid;                    // 가맹점 코드
    private String tid;                    // 결제 고유번호
    private String partner_order_id;       // 가맹점 주문번호
    private String partner_user_id;        // 가맹점 회원 ID
    private String pg_token;               // 결제승인 요청 인증 토큰
}