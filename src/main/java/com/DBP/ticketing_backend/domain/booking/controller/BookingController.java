package com.DBP.ticketing_backend.domain.booking.controller;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.booking.dto.BookingRequestDto;
import com.DBP.ticketing_backend.domain.booking.dto.BookingResponseDto;
import com.DBP.ticketing_backend.domain.booking.enums.BookingStatus;
//import com.DBP.ticketing_backend.domain.booking.facade.BookingFacade;
import com.DBP.ticketing_backend.domain.booking.service.BookingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = " 예매 api", description = "예매와 관련된 API 입니다.")
public class BookingController {

    private final BookingService bookingService;
    //private final BookingFacade bookingFacade;

    @Operation(
            summary = "예매 생성",
            description =
                    """
            이벤트 혹은 좌석 정보를 기반으로 예매를 생성합니다.

            **권한:**
            - 인증된 사용자만 접근 가능

            **참고:**
            - 요청 본문에 예매 정보를 포함해야 합니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예매 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @PostMapping
    public ResponseEntity<BookingResponseDto> createBooking(
            @AuthenticationPrincipal UsersDetails usersDetails,
            @RequestBody BookingRequestDto bookingRequestDto) {
        return ResponseEntity.ok(bookingService.createBooking(usersDetails, bookingRequestDto));
    }

    @Operation(
            summary = "예매 취소",
            description =
                    """
            결제 대기 상태 또는 예매 완료된 예매를 취소합니다.

            **권한:**
            - 인증된 사용자만 접근 가능

            **참고:**
            - 예매 ID를 경로 변수로 전달해야 합니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "예매 취소 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
        @ApiResponse(responseCode = "404", description = "예매를 찾을 수 없음")
    })
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> cancelBooking(
            @AuthenticationPrincipal UsersDetails usersDetails,
            @PathVariable("bookingId") Long bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(usersDetails, bookingId));
    }

    //    @Operation(
    //            summary = "결제",
    //            description =
    //                    """
    //            예매를 결제하여 확정합니다.
    //
    //            **권한:**
    //            - 인증된 사용자만 접근 가능
    //
    //            **참고:**
    //            - 예매 ID를 경로 변수로 전달해야 합니다.
    //            """)
    //    @ApiResponses({
    //        @ApiResponse(responseCode = "200", description = "결제 성공"),
    //        @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
    //        @ApiResponse(responseCode = "404", description = "예매를 찾을 수 없음")
    //    })
    //    @PostMapping("/{bookingId}")
    //    public ResponseEntity<BookingResponseDto> payBooking(
    //            @AuthenticationPrincipal UsersDetails usersDetails,
    //            @PathVariable("bookingId") Long bookingId) {
    //        return ResponseEntity.ok(bookingService.payBooking(usersDetails, bookingId));
    //    }

    @Operation(
            summary = "내 예매 내역 조회",
            description =
                    """
            로그인한 사용자의 예매 내역을 조회합니다.

            **권한:**
            - 인증된 사용자만 접근 가능

            **참고:**
            - `status` 파라미터를 사용하여 예매 상태별로 필터링할 수 있습니다.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponseDto>> getMyBookings(
            @AuthenticationPrincipal UsersDetails usersDetails,
            @RequestParam(required = false) BookingStatus status // ?status=CONFIRMED
            ) {
        return ResponseEntity.ok(bookingService.getMyBookings(usersDetails, status));
    }
}
