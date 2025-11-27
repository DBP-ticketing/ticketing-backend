package com.DBP.ticketing_backend.domain.payment.service;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.bookedseat.entity.BookedSeat;
import com.DBP.ticketing_backend.domain.bookedseat.repository.BookedSeatRepository;
import com.DBP.ticketing_backend.domain.booking.entity.Booking;
import com.DBP.ticketing_backend.domain.booking.enums.BookingStatus;
import com.DBP.ticketing_backend.domain.booking.repository.BookingRepository;
import com.DBP.ticketing_backend.domain.booking.service.BookingService;
import com.DBP.ticketing_backend.domain.payment.dto.response.KakaoPayApproveResponse;
import com.DBP.ticketing_backend.domain.payment.dto.response.KakaoPayReadyResponse;
import com.DBP.ticketing_backend.domain.payment.dto.response.PaymentResponseDto;
import com.DBP.ticketing_backend.domain.payment.entity.Payment;
import com.DBP.ticketing_backend.domain.payment.enums.PaymentStatus;
import com.DBP.ticketing_backend.domain.payment.repository.PaymentRepository;
import com.DBP.ticketing_backend.domain.queue.service.WaitingQueueService;
import com.DBP.ticketing_backend.domain.seat.entity.Seat;
import com.DBP.ticketing_backend.domain.seat.enums.SeatStatus;
import com.DBP.ticketing_backend.global.exception.CustomException;
import com.DBP.ticketing_backend.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final KakaoPayService kakaoPayService;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final WaitingQueueService waitingQueueService;
    private final BookingService bookingService;

    /** 결제 준비 - 카카오페이 결제 페이지 URL 반환 */
    @Transactional
    public PaymentResponseDto ready(UsersDetails usersDetails, Long bookingId) {

        // 1. 예약 조회 및 검증
        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() -> new CustomException(ErrorCode.BOOKING_NOT_FOUND));

        // 본인 예약인지 확인
        if (!booking.getUsers().getUserId().equals(usersDetails.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // PENDING 상태인지 확인
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_BOOKING_STATUS);
        }

        // 2. 좌석 정보 조회 (상품명 생성용)
        BookedSeat bookedSeat =
                bookedSeatRepository
                        .findByBooking(booking)
                        .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        Seat seat = bookedSeat.getSeat();
        String itemName =
                seat.getEvent().getEventName() + " - " + seat.getTemplate().getSection() + "석";

        // 3. 카카오페이 결제 준비 요청
        KakaoPayReadyResponse readyResponse =
                kakaoPayService.ready(
                        bookingId, usersDetails.getUserId(), itemName, booking.getTotalPrice());

        // 4. Payment 엔티티 생성 및 저장
        Payment payment =
                Payment.builder()
                        .booking(booking)
                        .tid(readyResponse.getTid())
                        .amount(booking.getTotalPrice())
                        .status(PaymentStatus.READY)
                        .build();

        paymentRepository.save(payment);

        // 5. 응답 DTO 생성
        return PaymentResponseDto.builder()
                .bookingId(bookingId)
                .tid(readyResponse.getTid())
                .paymentUrl(readyResponse.getNext_redirect_pc_url())
                .status(PaymentStatus.READY.name())
                .amount(booking.getTotalPrice())
                .build();
    }

    /** 결제 승인 - 사용자가 결제 완료 후 redirect되는 시점 */
    @Transactional
    public PaymentResponseDto approve(Long bookingId, String pgToken) {

        // 1. 예약 조회
        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() -> new CustomException(ErrorCode.BOOKING_NOT_FOUND));

        // 2. Payment 조회
        Payment payment =
                paymentRepository
                        .findByBooking(booking)
                        .orElseThrow(() -> new RuntimeException("결제 정보를 찾을 수 없습니다."));

        // 3. 카카오페이 결제 승인 요청
        KakaoPayApproveResponse approveResponse =
                kakaoPayService.approve(
                        payment.getTid(), bookingId, booking.getUsers().getUserId(), pgToken);

        // 4. Payment 상태 업데이트
        payment.approve(approveResponse.getPayment_method_type(), approveResponse.getApproved_at());

        // 5. Booking 상태 업데이트 (PENDING -> CONFIRMED)
        BookedSeat bookedSeat =
                bookedSeatRepository
                        .findByBooking(booking)
                        .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        Seat seat = bookedSeat.getSeat();

        // 좌석 상태 업데이트 (RESERVED -> SOLD)
        seat.updateStatus(SeatStatus.SOLD);

        bookingService.changeBookingStatusWithHistory(booking, bookedSeat, BookingStatus.CONFIRMED);

        // 6. 대기열에서 제거
        waitingQueueService.popQueue(seat.getEvent().getEventId(), booking.getUsers().getUserId());

        // 7. 응답 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return PaymentResponseDto.builder()
                .bookingId(bookingId)
                .tid(payment.getTid())
                .status(PaymentStatus.APPROVED.name())
                .amount(payment.getAmount())
                .approvedAt(approveResponse.getApproved_at().format(formatter))
                .build();
    }

    /** 결제 취소 - 사용자가 결제 중 취소 버튼 클릭 */
    @Transactional
    public void cancel(Long bookingId) {

        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() -> new CustomException(ErrorCode.BOOKING_NOT_FOUND));

        Payment payment = paymentRepository.findByBooking(booking).orElse(null);

        if (payment != null) {
            payment.cancel();
        }

        log.info("결제 취소 처리 완료 - bookingId: {}", bookingId);
    }

    /** 결제 실패 */
    @Transactional
    public void fail(Long bookingId) {

        Booking booking =
                bookingRepository
                        .findById(bookingId)
                        .orElseThrow(() -> new CustomException(ErrorCode.BOOKING_NOT_FOUND));

        Payment payment = paymentRepository.findByBooking(booking).orElse(null);

        if (payment != null) {
            payment.fail();
        }

        log.warn("결제 실패 처리 완료 - bookingId: {}", bookingId);
    }
}
