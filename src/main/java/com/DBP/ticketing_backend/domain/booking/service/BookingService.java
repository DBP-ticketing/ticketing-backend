package com.DBP.ticketing_backend.domain.booking.service;

import com.DBP.ticketing_backend.domain.auth.dto.UsersDetails;
import com.DBP.ticketing_backend.domain.bookedseat.entity.BookedSeat;
import com.DBP.ticketing_backend.domain.bookedseat.repository.BookedSeatRepository;
import com.DBP.ticketing_backend.domain.bookedseathistory.entity.BookedSeatHistory;
import com.DBP.ticketing_backend.domain.bookedseathistory.repository.BookedSeatHistoryRepository;
import com.DBP.ticketing_backend.domain.booking.dto.BookingRequestDto;
import com.DBP.ticketing_backend.domain.booking.dto.BookingResponseDto;
import com.DBP.ticketing_backend.domain.booking.entity.Booking;
import com.DBP.ticketing_backend.domain.booking.enums.BookingStatus;
import com.DBP.ticketing_backend.domain.booking.repository.BookingRepository;
import com.DBP.ticketing_backend.domain.event.entity.Event;
import com.DBP.ticketing_backend.domain.event.enums.EventStatus;
import com.DBP.ticketing_backend.domain.event.enums.SeatForm;
import com.DBP.ticketing_backend.domain.event.repository.EventRepository;
import com.DBP.ticketing_backend.domain.seat.entity.Seat;
import com.DBP.ticketing_backend.domain.seat.enums.SeatStatus;
import com.DBP.ticketing_backend.domain.seat.repository.SeatRepository;
import com.DBP.ticketing_backend.domain.users.entity.Users;
import com.DBP.ticketing_backend.domain.users.repository.UsersRepository;
import com.DBP.ticketing_backend.global.exception.CustomException;
import com.DBP.ticketing_backend.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final UsersRepository usersRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final BookingRepository bookingRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final BookedSeatHistoryRepository bookedSeatHistoryRepository;

    @Transactional
    public BookingResponseDto createBooking(UsersDetails usersDetails, BookingRequestDto bookingRequestDto) {

        // 유저 조회
        Users user = usersRepository.findById(usersDetails.getUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 이벤트 조회
        Long eventId = bookingRequestDto.getEventId();
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        // 예매 가능 시간 및 상태 확인
        validateTicketingTime(event);

        // 좌석 유형 확인
        SeatForm seatForm = event.getSeatForm();

        Seat seat;

        // 지정석의 경우
        if (seatForm == SeatForm.ASSIGNED) {
            if (bookingRequestDto.getSeatId() == null) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }

            seat = seatRepository.findByIdWithLock(bookingRequestDto.getSeatId())
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

            // 요청한 좌석의 이벤트와 일치하는지 확인
            if (!seat.getEvent().getEventId().equals(eventId)) {
                throw new CustomException(ErrorCode.INVALID_REQUEST);
            }
        // 자유석, 스탠딩석의 경우
        } else {
            seat = seatRepository.findFirstByEvent_EventIdAndStatus(eventId, SeatStatus.AVAILABLE)
                .orElseThrow(() -> new CustomException(ErrorCode.SOLD_OUT));
        }

        // 좌석 상태 확인 및 업데이트
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new CustomException(ErrorCode.ALREADY_RESERVED);
        }
        seat.updateStatus(SeatStatus.RESERVED);

        // 예약 생성
        Booking booking = Booking.builder()
            .users(user)
            .status(BookingStatus.PENDING) // 초기 상태: 결제 대기
            .totalPrice(seat.getPrice())
            .build();
        Booking savedBooking = bookingRepository.save(booking);

        // 예약된 좌석 정보 생성
        BookedSeat bookedSeat = BookedSeat.builder()
            .booking(savedBooking)
            .seat(seat)
            .build();
        BookedSeat savedBookedSeat = bookedSeatRepository.save(bookedSeat);

        // 예약된 좌석 이력 생성
        BookedSeatHistory history = BookedSeatHistory.builder()
            .bookedSeat(savedBookedSeat)
            .booking(savedBooking)
            .previousStatus(null) // 최초 생성이므로 이전 상태 없음
            .currentStatus(BookingStatus.PENDING)
            .build();

        bookedSeatHistoryRepository.save(history);

        return BookingResponseDto.from(savedBooking, seat);
    }

    @Transactional
    public BookingResponseDto cancelBooking(UsersDetails usersDetails, Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new CustomException(ErrorCode.BOOKING_NOT_FOUND));

        // 예약한 유저와 요청한 유저가 같은지 확인
        if (!booking.getUsers().getUserId().equals(usersDetails.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        BookedSeat bookedSeat = bookedSeatRepository.findByBooking(booking)
            .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        // 상태에 따른 분기 처리
        if (booking.getStatus() == BookingStatus.PENDING) {
            cancelPendingBooking(booking, bookedSeat);
        } else if (booking.getStatus() == BookingStatus.CONFIRMED) {
            cancelConfirmedBooking(booking, bookedSeat);
        } else {
            throw new CustomException(ErrorCode.INVALID_BOOKING_STATUS);
        }
        return BookingResponseDto.from(booking, bookedSeat.getSeat());
    }

    private void cancelConfirmedBooking(Booking booking, BookedSeat bookedSeat) {

        Seat seat = bookedSeat.getSeat();
        seat.updateStatus(SeatStatus.AVAILABLE);

        // 상태 변경 및 히스토리 저장
        changeBookingStatusWithHistory(booking, bookedSeat, BookingStatus.CANCELLED);
    }

    private void cancelPendingBooking(Booking booking, BookedSeat bookedSeat) {

        Seat seat = bookedSeat.getSeat();
        seat.updateStatus(SeatStatus.AVAILABLE);

        // 상태 변경 및 히스토리 저장
        changeBookingStatusWithHistory(booking, bookedSeat, BookingStatus.CANCELLED);
    }

    private void changeBookingStatusWithHistory(Booking booking, BookedSeat bookedSeat, BookingStatus newStatus) {
        // 1. 이전 상태 기록
        BookingStatus previousStatus = booking.getStatus();

        // 2. 상태 변경
        booking.updateStatus(newStatus);

        // 3. 히스토리 저장
        BookedSeatHistory history = BookedSeatHistory.builder()
            .bookedSeat(bookedSeat)
            .booking(booking)
            .previousStatus(previousStatus) // 변경 전 상태 (예: CONFIRMED)
            .currentStatus(newStatus)     // 변경 후 상태 (예: CANCELLED)
            .build();

        bookedSeatHistoryRepository.save(history);
    }

    @Transactional
    public BookingResponseDto payBooking(UsersDetails usersDetails, Long bookingId) {

        // 예약 조회
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new CustomException(ErrorCode.BOOKING_NOT_FOUND));

        // 예약한 유저와 요청한 유저가 같은지 확인
        if (!booking.getUsers().getUserId().equals(usersDetails.getUserId())) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            // 이미 결제되었거나, 취소된 예약은 결제 불가
            throw new CustomException(ErrorCode.INVALID_BOOKING_STATUS);
        }

        // 예약된 좌석 조회
        BookedSeat bookedSeat = bookedSeatRepository.findByBooking(booking)
            .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

        Seat seat = bookedSeat.getSeat();

        if (seat.getStatus() != SeatStatus.RESERVED) {
            throw new CustomException(ErrorCode.INVALID_SEAT_STATUS);
            // "결제 가능한 좌석 상태가 아닙니다."
        }

        // 좌석 상태를 SOLD로 업데이트
        seat.updateStatus(SeatStatus.SOLD);
        changeBookingStatusWithHistory(booking, bookedSeat, BookingStatus.CONFIRMED);

        return BookingResponseDto.from(booking, seat);
    }

    private void validateTicketingTime(Event event) {
        // 1. 시간 체크: 현재 시간이 예매 시작 시간보다 이전이면 에러
        if (LocalDateTime.now().isBefore(event.getTicketingStartAt())) {
            throw new CustomException(ErrorCode.TICKETING_NOT_OPEN);
        }

        // 2. 상태 체크: 이벤트 상태가 OPEN(예매 중)이 아니면 에러
        if (event.getStatus() != EventStatus.OPEN) {
            throw new CustomException(ErrorCode.TICKETING_CLOSED);
        }
    }

    @Transactional(readOnly = true) // 조회 전용 최적화
    public List<BookingResponseDto> getMyBookings(UsersDetails usersDetails, BookingStatus status) {

        Users user = usersRepository.findById(usersDetails.getUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        List<Booking> bookings;

        if (status == null) {
            bookings = bookingRepository.findAllByUsersOrderByCreatedAtDesc(user);
        } else {
            bookings = bookingRepository.findAllByUsersAndStatusOrderByCreatedAtDesc(user, status);
        }

        // Entity -> DTO 변환
        return bookings.stream()
            .map(booking -> {
                // BookedSeat를 통해 Seat 정보 가져오기
                BookedSeat bookedSeat = bookedSeatRepository.findByBooking(booking)
                    .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));

                return BookingResponseDto.from(booking, bookedSeat.getSeat());
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public void cancelExpiredBookings() {
        // 기준 시간 설정 (현재 시간 - 5분)
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

        // 만료된 예약들 조회 (PENDING 상태 & 5분 지남)
        List<Booking> expiredBookings = bookingRepository.findByStatusAndCreatedAtBefore(
            BookingStatus.PENDING,
            fiveMinutesAgo
        );

        // 하나씩 취소 처리
        for (Booking booking : expiredBookings) {
            BookedSeat bookedSeat = bookedSeatRepository.findByBooking(booking)
                .orElseThrow(() -> new CustomException(ErrorCode.SEAT_NOT_FOUND));
            cancelPendingBooking(booking, bookedSeat);
        }
    }
}
