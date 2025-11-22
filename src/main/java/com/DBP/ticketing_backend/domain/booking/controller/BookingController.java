package com.DBP.ticketing_backend.domain.booking.controller;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.booking.dto.BookingRequestDto;
import com.DBP.ticketing_backend.domain.booking.dto.BookingResponseDto;
import com.DBP.ticketing_backend.domain.booking.enums.BookingStatus;
import com.DBP.ticketing_backend.domain.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
@Tag(name = " 예매 api", description = "예매와 관련된 API 입니다.")
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "예매 생성", description = "이벤트 혹은 좌석 정보로 이벤트를 예매합니다.")
    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
        @AuthenticationPrincipal UsersDetails usersDetails,
        @RequestBody BookingRequestDto bookingRequestDto) {
        return ResponseEntity.ok(bookingService.createBooking(usersDetails, bookingRequestDto));
    }

    @Operation(summary = "예매 취소", description = "결제 대기 혹은 예매 완료된 예매를 취소합니다.")
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> cancelBooking(
        @AuthenticationPrincipal UsersDetails usersDetails,
        @PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(usersDetails, bookingId));
    }

    @Operation(summary = "결제", description = "결제를 통해 예매를 확정합니다.")
    @PostMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> payBooking(
        @AuthenticationPrincipal UsersDetails usersDetails,
        @PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok(bookingService.payBooking(usersDetails, bookingId));
    }

    @GetMapping("/my")
    @Operation(summary = "내 예매 내역 조회", description = "로그인한 유저의 예매 내역을 조회합니다. status 파라미터로 필터링 가능합니다.")
    public ResponseEntity<List<BookingResponseDto>> getMyBookings(
        @AuthenticationPrincipal UsersDetails usersDetails,
        @RequestParam(required = false) BookingStatus status // ?status=CONFIRMED
    ) {
        return ResponseEntity.ok(bookingService.getMyBookings(usersDetails, status));
    }
}
