package com.DBP.ticketing_backend.domain.seat.controller;

import com.DBP.ticketing_backend.domain.seat.dto.SeatResponseDto;
import com.DBP.ticketing_backend.domain.seat.service.SeatService;

import io.swagger.v3.oas.annotations.Operation;
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

    @GetMapping("/{eventId}")
    @Operation(summary = "좌석 조회", description = "특정 이벤트의 전체 좌석 상태를 조회합니다.")
    public ResponseEntity<List<SeatResponseDto>> getSeats(@PathVariable Long eventId) {
        return ResponseEntity.ok(seatService.getSeatsByEvent(eventId));
    }
}
