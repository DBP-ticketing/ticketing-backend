package com.DBP.ticketing_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.bookedseat.entity.BookedSeat;
import com.DBP.ticketing_backend.domain.bookedseat.repository.BookedSeatRepository;
import com.DBP.ticketing_backend.domain.bookedseathistory.entity.BookedSeatHistory;
import com.DBP.ticketing_backend.domain.bookedseathistory.repository.BookedSeatHistoryRepository;
import com.DBP.ticketing_backend.domain.booking.dto.BookingRequestDto;
import com.DBP.ticketing_backend.domain.booking.entity.Booking;
import com.DBP.ticketing_backend.domain.booking.facade.BookingFacade;
import com.DBP.ticketing_backend.domain.booking.repository.BookingRepository;
import com.DBP.ticketing_backend.domain.seat.entity.Seat;
import com.DBP.ticketing_backend.domain.seat.enums.SeatStatus;
import com.DBP.ticketing_backend.domain.seat.repository.SeatRepository;
import com.DBP.ticketing_backend.domain.users.entity.Users;
import com.DBP.ticketing_backend.domain.users.enums.UsersRole;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@TestPropertySource(
        properties = {
            "jwt.secret=dGhpcyBpcyBhIHZlcnkgbG9uZyBzZWNyZXQga2V5IGZvciBqd3QgdGVzdGluZw==",
            "jwt.access-token-expiration=3600000",
            "jwt.refresh-token-expiration=86400000"
        })
public class BookingConcurrencyTest {

    @Autowired private BookingFacade bookingFacade;
    @Autowired private BookedSeatRepository bookedSeatRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private BookedSeatHistoryRepository bookedSeatHistoryRepository;
    @Autowired private SeatRepository seatRepository;

    @Test
    void 지정석_동시예매_100명_테스트() throws InterruptedException {
        // given

        UsersDetails mockUser =
                new UsersDetails(Users.builder().userId(3L).role(UsersRole.ROLE_USER).build());

        int threadCount = 100; // 100명이 동시에
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 성공 횟수, 실패 횟수 카운터
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // 테스트용 요청 DTO
        BookingRequestDto request =
                BookingRequestDto.builder()
                        .eventId(1L) // 9999년에 진행되는 이벤트
                        .seatId(2001L)
                        .build();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(
                    () -> {
                        try {
                            bookingFacade.createBooking(mockUser, request);
                            successCount.getAndIncrement(); // 성공하면 +1
                        } catch (Exception e) {
                            failCount.getAndIncrement(); // 실패(이미 예약됨)하면 +1
                            System.out.println("예매 실패: " + e.getMessage());
                        } finally {
                            latch.countDown(); // 스레드 하나 끝남
                        }
                    });
        }

        latch.await(); // 100명이 다 끝날 때까지 대기

        // then
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());

        // 검증: 오직 1명만 성공해야 함
        assertEquals(1, successCount.get());
        // 검증: 99명은 실패해야 함
        assertEquals(99, failCount.get());
    }

    @AfterEach
    void tearDown() {
        // 1. 테스트에 사용한 좌석 ID
        Long targetSeatId = 2001L;

        // 2. 해당 좌석을 조회
        Seat seat = seatRepository.findById(targetSeatId).orElseThrow();

        // 3. 좌석 상태를 'AVAILABLE'로 원상복구
        seat.updateStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);

        // 4. 테스트로 인해 생긴 예매 내역 삭제
        List<BookedSeat> bookedSeats =
                bookedSeatRepository.findAll().stream()
                        .filter(bs -> bs.getSeat().getSeatId().equals(targetSeatId))
                        .toList();

        for (BookedSeat bs : bookedSeats) {
            // History 삭제
            List<BookedSeatHistory> histories =
                    bookedSeatHistoryRepository.findAll().stream()
                            .filter(
                                    h ->
                                            h.getBookedSeat()
                                                    .getBookedSeatId()
                                                    .equals(bs.getBookedSeatId()))
                            .toList();
            bookedSeatHistoryRepository.deleteAll(histories);

            // Booking 찾기
            Booking booking = bs.getBooking();

            // BookedSeat 삭제
            bookedSeatRepository.delete(bs);

            // Booking 삭제
            bookingRepository.delete(booking);
        }
    }
}
