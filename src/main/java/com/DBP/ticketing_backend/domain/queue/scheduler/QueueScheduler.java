package com.DBP.ticketing_backend.domain.queue.scheduler;

import com.DBP.ticketing_backend.domain.event.entity.Event;
import com.DBP.ticketing_backend.domain.event.enums.EventStatus;
import com.DBP.ticketing_backend.domain.event.repository.EventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueueScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final EventRepository eventRepository;

    private static final long MAX_USERS = 100L; // 최대 동시 접속 인원
    private static final long ACTIVE_USER_TTL = 5 * 60 * 1000; // 5분
    private static final long WAITING_USER_TTL = 60 * 60 * 1000; // 1시간

    @Scheduled(fixedDelay = 1000) // 1초마다 실행
    public void scheduleQueue() {
        // OPEN 상태인 이벤트만 관리
        List<Event> openEvents = eventRepository.findAllByStatus(EventStatus.OPEN);

        for (Event event : openEvents) {
            Long eventId = event.getEventId();
            String waitingKey = "waiting_queue:" + eventId;
            String activeKey = "active_tokens:" + eventId;

            long now = System.currentTimeMillis();

            // =====================================================
            // 1. [청소] Waiting Queue: 1시간 넘게 대기한 유저 삭제 (Ghost 제거)
            // =====================================================
            long waitingCutoff = now - WAITING_USER_TTL;
            redisTemplate.opsForZSet().removeRangeByScore(waitingKey, 0, waitingCutoff);

            // =====================================================
            // 2. [청소] Active Queue: 5분 지난 Active 유저 자동 퇴장 (Time-out)
            // =====================================================
            long activeCutoff = now - ACTIVE_USER_TTL;
            Long evictedCount =
                    redisTemplate.opsForZSet().removeRangeByScore(activeKey, 0, activeCutoff);

            if (evictedCount != null && evictedCount > 0) {
                log.info("Event {} : 시간 초과로 {}명 자동 퇴장 처리됨.", eventId, evictedCount);
            }

            // =====================================================
            // 3. [입장] 빈자리만큼 대기열에서 입장 (Move Waiting -> Active)
            // =====================================================
            // 현재 Active 인원 확인
            Long currentActive = redisTemplate.opsForZSet().zCard(activeKey);
            if (currentActive == null) currentActive = 0L;

            long availableSlots = MAX_USERS - currentActive;

            // 자리가 남았고, 대기자가 있다면 입장
            if (availableSlots > 0) {
                // 대기열 앞에서부터 가져오기
                Set<String> userIds =
                        redisTemplate.opsForZSet().range(waitingKey, 0, availableSlots - 1);

                if (userIds != null && !userIds.isEmpty()) {
                    for (String userId : userIds) {
                        // 트랜잭션 처리 권장되나, 간단히 순차 실행

                        // A. 대기열 제거
                        redisTemplate.opsForZSet().remove(waitingKey, userId);

                        // B. 활성열 추가 (Score = 현재 시간 -> 5분 타이머 시작)
                        redisTemplate
                                .opsForZSet()
                                .add(activeKey, userId, System.currentTimeMillis());

                        log.info(
                                "Event {} : 유저 {} 입장 성공! (현재 접속자: {}명)",
                                eventId,
                                userId,
                                currentActive + 1);
                        currentActive++;
                    }
                }
            }
        }
    }
}
