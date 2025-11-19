package com.DBP.ticketing_backend.domain.seat.repository;

import com.DBP.ticketing_backend.domain.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {

}
