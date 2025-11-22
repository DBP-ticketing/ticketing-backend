package com.DBP.ticketing_backend.domain.bookedseat.repository;

import com.DBP.ticketing_backend.domain.bookedseat.entity.BookedSeat;
import com.DBP.ticketing_backend.domain.booking.entity.Booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookedSeatRepository extends JpaRepository<BookedSeat, Long> {

    Optional<BookedSeat> findByBooking(Booking booking);
}
