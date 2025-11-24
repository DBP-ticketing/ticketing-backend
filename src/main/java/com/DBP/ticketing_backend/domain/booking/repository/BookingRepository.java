package com.DBP.ticketing_backend.domain.booking.repository;

import com.DBP.ticketing_backend.domain.booking.entity.Booking;
import com.DBP.ticketing_backend.domain.booking.enums.BookingStatus;
import com.DBP.ticketing_backend.domain.users.entity.Users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // 내 예약 전체 조회 (최신순)
    List<Booking> findAllByUsersOrderByCreatedAtDesc(Users users);

    // 내 예약 상태별 조회 (최신순)
    List<Booking> findAllByUsersAndStatusOrderByCreatedAtDesc(Users users, BookingStatus status);

    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime threshold);
}
