package com.DBP.ticketing_backend.domain.payment.service;

import com.DBP.ticketing_backend.domain.payment.dto.request.KakaoPayApproveRequest;
import com.DBP.ticketing_backend.domain.payment.dto.request.KakaoPayReadyRequest;
import com.DBP.ticketing_backend.domain.payment.dto.response.KakaoPayApproveResponse;
import com.DBP.ticketing_backend.domain.payment.dto.response.KakaoPayReadyResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoPayService {

    @Value("${kakaopay.secretKey}")
    private String secretKey;

    @Value("${kakaopay.cid}")
    private String cid;

    private static final String KAKAO_PAY_READY_URL =
            "https://open-api.kakaopay.com/online/v1/payment/ready";
    private static final String KAKAO_PAY_APPROVE_URL =
            "https://open-api.kakaopay.com/online/v1/payment/approve";

    /** 카카오페이 결제 준비 (1단계) */
    public KakaoPayReadyResponse ready(
            Long bookingId, Long userId, String itemName, Integer totalAmount) {

        // 콜백 URL 동적 생성
        String baseUrl = "http://localhost:8080/api/payment";
        String approvalUrl = baseUrl + "/success?booking_id=" + bookingId;
        String cancelUrl = baseUrl + "/cancel?booking_id=" + bookingId;
        String failUrl = baseUrl + "/fail?booking_id=" + bookingId;

        KakaoPayReadyRequest request =
                KakaoPayReadyRequest.builder()
                        .cid(cid)
                        .partner_order_id(String.valueOf(bookingId))
                        .partner_user_id(String.valueOf(userId))
                        .item_name(itemName)
                        .quantity(1)
                        .total_amount(totalAmount)
                        .tax_free_amount(0)
                        .approval_url(approvalUrl)
                        .cancel_url(cancelUrl)
                        .fail_url(failUrl)
                        .build();

        log.info("카카오페이 결제 준비 요청 - bookingId: {}, amount: {}", bookingId, totalAmount);

        KakaoPayReadyResponse response =
                WebClient.create(KAKAO_PAY_READY_URL)
                        .post()
                        .header(HttpHeaders.AUTHORIZATION, "SECRET_KEY " + secretKey)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(KakaoPayReadyResponse.class)
                        .block();

        log.info("카카오페이 결제 준비 성공 - tid: {}", response.getTid());

        return response;
    }

    /** 카카오페이 결제 승인 (2단계) */
    public KakaoPayApproveResponse approve(
            String tid, Long bookingId, Long userId, String pgToken) {

        KakaoPayApproveRequest request =
                KakaoPayApproveRequest.builder()
                        .cid(cid)
                        .tid(tid)
                        .partner_order_id(String.valueOf(bookingId))
                        .partner_user_id(String.valueOf(userId))
                        .pg_token(pgToken)
                        .build();

        log.info("카카오페이 결제 승인 요청 - tid: {}, bookingId: {}", tid, bookingId);

        KakaoPayApproveResponse response =
                WebClient.create(KAKAO_PAY_APPROVE_URL)
                        .post()
                        .header(HttpHeaders.AUTHORIZATION, "SECRET_KEY " + secretKey)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(KakaoPayApproveResponse.class)
                        .block();

        log.info("카카오페이 결제 승인 성공 - aid: {}", response.getAid());

        return response;
    }
}
