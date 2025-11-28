package com.DBP.ticketing_backend.domain.payment.controller;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.payment.dto.response.PaymentResponseDto;
import com.DBP.ticketing_backend.domain.payment.service.PaymentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Tag(name = " 결제 API", description = "카카오페이 결제 관련 API")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "결제 준비",
            description =
                    """
    예약에 대한 카카오페이 결제를 준비하고 결제 페이지 URL을 반환합니다.

    **권한:**
    - 인증된 사용자만 접근 가능

    **참고:**
    - 예약 ID를 경로 변수로 전달해야 합니다.
    - 반환된 paymentUrl로 리다이렉트하여 결제를 진행합니다.
    """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 준비 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
        @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
    })
    @PostMapping("/ready/{bookingId}")
    public ResponseEntity<PaymentResponseDto> ready(
            @PathVariable Long bookingId, @AuthenticationPrincipal UsersDetails usersDetails) {

        log.info("결제 준비 요청 - bookingId: {}, userId: {}", bookingId, usersDetails.getUserId());

        PaymentResponseDto response = paymentService.ready(usersDetails, bookingId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "결제 승인",
            description =
                    """
    카카오페이 결제 승인을 처리합니다. (사용자가 결제 완료 후 리다이렉트되는 엔드포인트)

    **참고:**
    - 카카오페이에서 자동으로 리다이렉트되며 pg_token이 전달됩니다.
    - 결제 완료 후 예약 상태가 CONFIRMED로 변경됩니다.
    """)
    @GetMapping("/success")
    public RedirectView success(
            @RequestParam("pg_token") String pgToken, @RequestParam("booking_id") Long bookingId) {

        log.info("결제 승인 콜백 - bookingId: {}, pgToken: {}", bookingId, pgToken);

        try {
            paymentService.approve(bookingId, pgToken);

            // 프론트엔드 성공 페이지로 리다이렉트
            return new RedirectView(
                    "http://localhost:3000/payment/success?booking_id=" + bookingId);

        } catch (Exception e) {
            log.error("결제 승인 실패", e);
            return new RedirectView("http://localhost:3000/payment/fail?booking_id=" + bookingId);
        }
    }

    @Operation(summary = "결제 취소", description = "사용자가 결제 중 취소 버튼을 클릭한 경우 호출됩니다.")
    @GetMapping("/cancel")
    public RedirectView cancel(@RequestParam("booking_id") Long bookingId) {

        log.info("결제 취소 콜백 - bookingId: {}", bookingId);

        paymentService.cancel(bookingId);

        // 프론트엔드 취소 페이지로 리다이렉트
        return new RedirectView("http://localhost:3000/payment/cancel?booking_id=" + bookingId);
    }

    @Operation(summary = "결제 실패", description = "결제 중 오류가 발생한 경우 호출됩니다.")
    @GetMapping("/fail")
    public RedirectView fail(@RequestParam("booking_id") Long bookingId) {

        log.warn("결제 실패 콜백 - bookingId: {}", bookingId);

        paymentService.fail(bookingId);

        // 프론트엔드 실패 페이지로 리다이렉트
        return new RedirectView("http://localhost:3000/payment/fail?booking_id=" + bookingId);
    }
}
