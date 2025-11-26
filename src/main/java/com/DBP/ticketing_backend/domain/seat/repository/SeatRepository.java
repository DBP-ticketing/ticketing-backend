package com.DBP.ticketing_backend.domain.seat.repository;

import com.DBP.ticketing_backend.domain.seat.entity.Seat;
import com.DBP.ticketing_backend.domain.seat.enums.SeatStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Query(
            "SELECT s FROM Seat s WHERE s.event.eventId = :eventId ORDER BY s.level,"
                    + " s.template.row, s.template.column")
    List<Seat> findAllByEventId(@Param("eventId") Long eventId);

    //    @Lock(LockModeType.PESSIMISTIC_WRITE)
    //    @Query("select s from Seat s where s.seatId = :seatId")
    //    Optional<Seat> findByIdWithLock(Long seatId);

    Optional<Seat> findFirstByEvent_EventIdAndStatus(Long eventId, SeatStatus seatStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            "select s from Seat s where s.event.eventId = :eventId and s.status = :status order by"
                    + " s.seatId asc limit 1")
    Optional<Seat> findFirstByEvent_EventIdAndStatusOrderBySeatIdAscWithLock(
            @Param("eventId") Long eventId, @Param("status") SeatStatus status);
}
