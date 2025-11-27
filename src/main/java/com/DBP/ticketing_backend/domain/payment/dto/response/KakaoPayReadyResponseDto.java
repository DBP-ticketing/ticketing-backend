package com.DBP.ticketing_backend.domain.payment.dto.response;

import lombok.Data;

@Data
public class KakaoPayReadyResponseDto {
    private String tid;                          // 결제 고유번호
    private String next_redirect_pc_url;         // PC 웹일 경우 redirect URL
    private String next_redirect_mobile_url;     // 모바일 웹일 경우 redirect URL
    private String created_at;
}
