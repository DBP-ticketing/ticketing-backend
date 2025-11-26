package com.DBP.ticketing_backend.domain.queue.controller;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.queue.dto.QueueResponseDto;
import com.DBP.ticketing_backend.domain.queue.service.WaitingQueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
@Tag(name = " 대기열 API", description = "대기열 진입 및 순서 확인 API")
public class QueueController {

    private final WaitingQueueService waitingQueueService;

    @Operation(
        summary = "대기열 진입",
        description =
            """
            특정 이벤트의 대기열에 줄을 섭니다.

            **권한:**
            - 인증된 사용자만 접근 가능

            **참고:**
            - 이벤트 ID가 유효하지 않으면 404 에러가 반환됩니다.
            - 이미 대기열에 등록된 경우 중복 등록은 허용되지 않습니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "대기열 등록 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
        @ApiResponse(responseCode = "404", description = "이벤트를 찾을 수 없음")
    })
    @PostMapping("/{eventId}")
    public ResponseEntity<String> joinQueue(
        @PathVariable Long eventId,
        @AuthenticationPrincipal UsersDetails usersDetails) {

        waitingQueueService.registerQueue(eventId, usersDetails.getUserId());
        return ResponseEntity.ok("대기열에 등록되었습니다.");
    }

    @Operation(
        summary = "내 순서 확인",
        description =
            """
            현재 내 대기 순번과 입장 가능 여부를 조회합니다.

            **반환 정보:**
            - 현재 대기 순번
            - 입장 가능 여부 (true/false)

            **권한:**
            - 인증된 사용자만 접근 가능

            **참고:**
            - 이벤트 ID가 유효하지 않으면 404 에러가 반환됩니다.
            - 대기열에 등록되지 않은 경우 순번은 null로 반환됩니다.
            """
    )    @GetMapping("/rank/{eventId}")
    public ResponseEntity<QueueResponseDto> getRank(
        @PathVariable Long eventId,
        @AuthenticationPrincipal UsersDetails usersDetails) {

        Long rank = waitingQueueService.getRank(eventId, usersDetails.getUserId());
        boolean isActive = waitingQueueService.isActive(eventId, usersDetails.getUserId());

        return ResponseEntity.ok(new QueueResponseDto(rank, isActive));
    }
}
