package com.DBP.ticketing_backend.domain.queue.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WaitingQueueService {

    private final RedisTemplate<String, String> redisTemplate;

    // 1. 대기열 등록
    public void registerQueue(Long eventId, Long userId) {
        String key = "waiting_queue:" + eventId;
        long timestamp = System.currentTimeMillis();

        // ZADD key score member (점수는 접속 시간)
        // 이미 줄을 선 유저라면 기존 점수 유지
        redisTemplate.opsForZSet().add(key, userId.toString(), timestamp);
    }

    // 2. 내 순서 조회
    public Long getRank(Long eventId, Long userId) {
        String key = "waiting_queue:" + eventId;
        Long rank = redisTemplate.opsForZSet().rank(key, userId.toString());

        // 0등부터 시작하므로 +1 처리
        return (rank != null) ? rank + 1 : null;
    }

    // 3. 입장 가능 여부 확인 (isActive)
    // 인터셉터에서 호출됨
    public boolean isActive(Long eventId, Long userId) {
        String key = "active_tokens:" + eventId;
        // 점수(입장시간)가 존재하면 Active 상태임
        Double score = redisTemplate.opsForZSet().score(key, userId.toString());
        return score != null;
    }

    // 4. 퇴장 처리 (결제/취소 시 호출)
    public void popQueue(Long eventId, Long userId) {
        String key = "active_tokens:" + eventId;
        redisTemplate.opsForZSet().remove(key, userId.toString());
    }
}
