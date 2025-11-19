package com.DBP.ticketing_backend.domain.event.controller;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.event.dto.CreateEventRequestDto;
import com.DBP.ticketing_backend.domain.event.dto.EventDetailResponseDto;
import com.DBP.ticketing_backend.domain.event.dto.EventListResponseDto;
import com.DBP.ticketing_backend.domain.event.enums.EventStatus;
import com.DBP.ticketing_backend.domain.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = " 이벤트 api", description = "이벤트 API 입니다.")
public class EventController {

    private final EventService eventService;

    @Operation(summary = "이벤트 생성", description = "호스트가 이벤트 정보를 입력하여 이벤트를 생성합니다.")
    @PostMapping
    public ResponseEntity<Long> createEvent(
        @RequestBody CreateEventRequestDto createEventRequestDto,
        @AuthenticationPrincipal UsersDetails usersDetails) {
        return ResponseEntity.ok(eventService.createEvent(createEventRequestDto, usersDetails));
    }

    @Operation(summary = "이벤트 조회", description = "이벤트 상태에 따라 조회합니다")
    @GetMapping
    public ResponseEntity<List<EventListResponseDto>> getEvents(@RequestParam(required = false) EventStatus status) {
        return ResponseEntity.ok(eventService.getEvents(status));
    }

    @Operation(summary = "이벤트 조회", description = "이벤트 아이디를 통해 이벤트를 조회합니다.")
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailResponseDto> getEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEvent(eventId));
    }
}