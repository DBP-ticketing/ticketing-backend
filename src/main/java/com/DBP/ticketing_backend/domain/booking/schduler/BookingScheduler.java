package com.DBP.ticketing_backend.domain.booking.schduler;

import com.DBP.ticketing_backend.domain.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingService bookingService;

    @Scheduled(cron = "0 * * * * *") // 매 분 0초에 실행
    public void autoCancelUnpaidBookings() {
        log.info("결제 미진행 예약 자동 취소 스케줄러 실행");
        bookingService.cancelExpiredBookings();
    }

}
