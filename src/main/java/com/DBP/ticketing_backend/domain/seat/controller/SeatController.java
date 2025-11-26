package com.DBP.ticketing_backend.domain.seat.controller;

import com.DBP.ticketing_backend.domain.seat.dto.SeatResponseDto;
import com.DBP.ticketing_backend.domain.seat.service.SeatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seat")
@RequiredArgsConstructor
@Tag(name = " 좌석 api", description = "좌석과 관련된 API 입니다.")
public class SeatController {

    private final SeatService seatService;

    @Operation(
        summary = "좌석 조회",
        description =
            """
            특정 이벤트의 전체 좌석 상태를 조회합니다.

            **권한:**
            - 모든 사용자 접근 가능

            **참고:**
            - 이벤트 ID가 유효하지 않으면 404 에러가 반환됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = SeatResponseDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    @GetMapping("/{eventId}")
    public ResponseEntity<List<SeatResponseDto>> getSeats(@PathVariable Long eventId) {
        return ResponseEntity.ok(seatService.getSeatsByEvent(eventId));
    }
}
