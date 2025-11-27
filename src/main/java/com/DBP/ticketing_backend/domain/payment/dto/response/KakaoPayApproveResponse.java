package com.DBP.ticketing_backend.domain.payment.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KakaoPayApproveResponse {
    private String aid;                    // 요청 고유 번호
    private String tid;                    // 결제 고유 번호
    private String cid;                    // 가맹점 코드
    private String partner_order_id;       // 가맹점 주문번호
    private String partner_user_id;        // 가맹점 회원 ID
    private String payment_method_type;    // 결제 수단
    private Amount amount;                 // 결제 금액 정보
    private String item_name;              // 상품명
    private Integer quantity;              // 상품 수량
    private LocalDateTime created_at;      // 결제 준비 요청 시각
    private LocalDateTime approved_at;     // 결제 승인 시각

    @Data
    public static class Amount {
        private Integer total;             // 전체 결제 금액
        private Integer tax_free;          // 비과세 금액
        private Integer vat;               // 부가세 금액
        private Integer point;             // 사용한 포인트
        private Integer discount;          // 할인 금액
    }
}
