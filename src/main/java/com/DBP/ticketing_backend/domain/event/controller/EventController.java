package com.DBP.ticketing_backend.domain.event.controller;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.event.dto.CreateEventRequestDto;
import com.DBP.ticketing_backend.domain.event.dto.EventDetailResponseDto;
import com.DBP.ticketing_backend.domain.event.dto.EventListResponseDto;
import com.DBP.ticketing_backend.domain.event.enums.EventStatus;
import com.DBP.ticketing_backend.domain.event.service.EventService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = " 이벤트 api", description = "이벤트 API 입니다.")
public class EventController {

    private final EventService eventService;

    @Operation(
        summary = "이벤트 생성",
        description =
            """
            호스트가 이벤트 정보를 입력하여 새로운 이벤트를 생성합니다.

            **권한:**
            - 인증된 사용자만 접근 가능

            **참고:**
            - 요청 본문에 이벤트 정보를 포함해야 합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "이벤트 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @PostMapping
    public ResponseEntity<Long> createEvent(
            @RequestBody CreateEventRequestDto createEventRequestDto,
            @AuthenticationPrincipal UsersDetails usersDetails) {
        return ResponseEntity.ok(eventService.createEvent(createEventRequestDto, usersDetails));
    }

    @Operation(
        summary = "이벤트 목록 조회",
        description =
            """
            이벤트 상태에 따라 이벤트 목록을 조회합니다.

            **권한:**
            - 모든 사용자 접근 가능

            **참고:**
            - `status` 파라미터를 사용하여 특정 상태의 이벤트만 필터링할 수 있습니다.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = EventListResponseDto.class))
        ),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @GetMapping
    public ResponseEntity<List<EventListResponseDto>> getEvents(
            @RequestParam(required = false) EventStatus status) {
        return ResponseEntity.ok(eventService.getEvents(status));
    }

    @Operation(
        summary = "이벤트 상세 조회",
        description =
            """
            이벤트 ID를 사용하여 특정 이벤트의 상세 정보를 조회합니다.

            **권한:**
            - 모든 사용자 접근 가능

            **참고:**
            - 유효하지 않은 이벤트 ID를 전달하면 404 에러가 반환됩니다.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = EventDetailResponseDto.class))
        ),
        @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailResponseDto> getEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEvent(eventId));
    }
}
