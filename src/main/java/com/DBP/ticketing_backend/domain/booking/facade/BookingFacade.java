package com.DBP.ticketing_backend.domain.booking.facade;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.booking.dto.BookingRequestDto;
import com.DBP.ticketing_backend.domain.booking.dto.BookingResponseDto;
import com.DBP.ticketing_backend.domain.booking.service.BookingService;
import com.DBP.ticketing_backend.global.exception.CustomException;
import com.DBP.ticketing_backend.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingFacade {

    private final RedissonClient redissonClient;
    private final BookingService bookingService;

    public BookingResponseDto createBooking(
            UsersDetails usersDetails, BookingRequestDto requestDto) {

        // 1. 지정석 예매인 경우 (seatId가 존재함) -> 분산 락 적용
        if (requestDto.getSeatId() != null) {
            return createAssignedBookingWithLock(usersDetails, requestDto);
        }

        // 2. 스탠딩/자유석 예매인 경우 -> 락 없이 서비스 호출 (추후 대기열/재고 차감 로직 적용 예정)
        return bookingService.createBooking(usersDetails, requestDto);
    }

    private BookingResponseDto createAssignedBookingWithLock(
            UsersDetails usersDetails, BookingRequestDto requestDto) {
        Long seatId = requestDto.getSeatId();

        // 락 Key 생성 (유니크)
        String lockKey = "lock:seat:" + seatId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 시도 (tryLock)
            // waitTime (3초): 락을 얻기 위해 기다리는 시간 (3초 동안 못 얻으면 포기)
            // leaseTime (10초): 락을 얻은 후 유지하는 시간 (10초 지나면 강제 반납 - 데드락 방지)
            boolean available = lock.tryLock(3, 10, TimeUnit.SECONDS);

            if (!available) {
                log.warn("좌석 락 획득 실패 - seatId: {}", seatId);
                throw new CustomException(ErrorCode.ALREADY_RESERVED); // "이미 처리 중인 좌석입니다."
            }

            log.info("좌석 락 획득 성공 - seatId: {}", seatId);

            return bookingService.createBooking(usersDetails, requestDto);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 락 해제 (반드시 finally 블록에서 수행)
            // isHeldByCurrentThread: 현재 스레드가 락을 잡고 있을 때만 해제 (남의 락 해제 방지)
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("좌석 락 해제 완료 - seatId: {}", seatId);
            }
        }
    }
}
