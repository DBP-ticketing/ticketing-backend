package com.DBP.ticketing_backend.global.interceptor;

import com.DBP.ticketing_backend.domain.queue.service.WaitingQueueService;
import com.DBP.ticketing_backend.global.exception.CustomException;
import com.DBP.ticketing_backend.global.exception.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueTokenInterceptor implements HandlerInterceptor {

    private final WaitingQueueService waitingQueueService;

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Preflight 요청(OPTIONS)은 통과
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 1. 헤더 추출 (프론트엔드가 보내줘야 함)
        String userIdStr = request.getHeader("Queue-Token"); // 유저 ID (토큰 대신 사용)
        String eventIdStr = request.getHeader("Event-Id"); // 이벤트 ID

        // 2. 헤더 누락 검사
        if (!StringUtils.hasText(userIdStr) || !StringUtils.hasText(eventIdStr)) {
            throw new CustomException(ErrorCode.INVALID_QUEUE_TOKEN);
        }

        try {
            Long userId = Long.valueOf(userIdStr);
            Long eventId = Long.valueOf(eventIdStr);

            // 3. Redis 검증 (Active 목록에 있는지 확인)
            if (!waitingQueueService.isActive(eventId, userId)) {
                throw new CustomException(ErrorCode.QUEUE_TOKEN_EXPIRED);
            }
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_QUEUE_TOKEN);
        }

        return true; // 통과
    }
}
